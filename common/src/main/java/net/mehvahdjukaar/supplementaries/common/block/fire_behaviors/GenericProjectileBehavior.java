package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.IEntityInterceptFakeLevel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GenericProjectileBehavior implements IBallisticBehavior {

    @Override
    public Data calculateData(ItemStack projectile, Level level) {
        if (projectile.isEmpty()) {
            return IBallisticBehavior.LINE;
        }
        Entity proj = createEntity(projectile, IEntityInterceptFakeLevel.get(level), new Vec3(1, 0, 0));
        if (proj != null) {
            double speed = proj.getDeltaMovement().length();
            if (speed == 0 && proj instanceof AbstractArrow) {
                //TODO;
                speed = 2; //crossbow is 3..
            }

            proj.setDeltaMovement(1, 0, 0);
            proj.tick();
            var newMovement = proj.getDeltaMovement();
            float drag = (float) newMovement.x;
            float gravity = (float) -newMovement.y;
            return new Data(drag, gravity, (float) speed);
        }
        return IBallisticBehavior.LINE;
    }

    @Override
    public boolean fireInner(ItemStack stack, ServerLevel level, Vec3 firePos,
                             Vec3 facing, float scalePower, int inaccuracy, @Nullable Player owner) {
        Entity entity = createEntity(stack, IEntityInterceptFakeLevel.get(level), facing);

        if (entity != null) {
            //create new one just to be sure. Need since it might have been created in a different level
            entity = MiscUtils.cloneEntity(entity, level);
            if (entity != null) {

                // setup entity
                if (entity instanceof Projectile pr) {
                    pr.cachedOwner = null;
                    pr.ownerUUID = null;
                    pr.setOwner(owner);

                    pr.shoot(facing.x, facing.y, facing.z,
                            scalePower, inaccuracy);
                }else{
                    entity.setDeltaMovement(facing.scale(scalePower));
                }

                //  float radius = entity.getBbWidth() * 1.42f;
                //firePos = firePos.add(facing.normalize().scale(radius));
                entity.setPos(firePos.x, firePos.y, firePos.z);

                level.addFreshEntity(entity);

                return true;
            }
        }
        return false;
    }

    private static final GameProfile FAKE_PLAYER = new GameProfile(UUID.fromString("11242C44-14d5-1f22-3d27-13D2C45CA355"), "[CANNON_TESTER]");

    // facing is likely not needed
    @Nullable
    @VisibleForTesting
    public Entity createEntity(ItemStack projectile, IEntityInterceptFakeLevel testLevel, Vec3 facing) {

        Level fakeLevel = testLevel.cast();
        // fake player living in fake level
        Player fakePlayer = FakePlayerManager.get(FAKE_PLAYER, fakeLevel);

        fakePlayer.setXRot((float) MthUtils.getPitch(facing));
        fakePlayer.setYRot((float) MthUtils.getYaw(facing));

        testLevel.setup();

        if (projectile.getItem() instanceof ArrowItem ai) {
            return ai.createArrow(fakeLevel, projectile, fakePlayer, null);
        }
        // let player shoot them instead.
        //TODO: check.
        // this is mostly relevant for stuff like potions which has way more power in dispensers for some reason
        else if (projectile.getItem() instanceof ProjectileItem ti) {
            var config = ti.createDispenseConfig();
            //apply config
            var p = ti.asProjectile(fakeLevel, Vec3.ZERO, projectile, Direction.UP);
            float power = config.power();
            //hardcoded bs for potions since hand throw and dispenser throw are so different
            if (p instanceof ThrownPotion) {
                power *= 0.75f;
            }
            p.shoot(0, 1, 0, power, config.uncertainty());
            return p;
        }
        //create from item

        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, projectile.copy());

        var eventResult = SuppPlatformStuff.fireItemRightClickEvent(fakePlayer, InteractionHand.MAIN_HAND);
        if (!eventResult.getResult().consumesAction()) {
            projectile.use(fakeLevel, fakePlayer, InteractionHand.MAIN_HAND);
        }

        return testLevel.getIntercepted();
    }


}
