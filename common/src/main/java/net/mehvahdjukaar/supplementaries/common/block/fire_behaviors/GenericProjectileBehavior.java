package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.ProjectileTestLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
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
        Entity proj = createEntity(projectile, ProjectileTestLevel.get(level.registryAccess()), new Vec3(1,0,0));
        if (proj != null) {
            double speed = proj.getDeltaMovement().length();
            if(speed == 0 && proj instanceof AbstractArrow){
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
        Entity entity = createEntity(stack, ProjectileTestLevel.get(level.registryAccess()), facing);

        if (entity != null) {

            //create new one just to be sure
            CompoundTag c = new CompoundTag();
            entity.save(c);
            var opt = EntityType.create(c, level); // create new to reset level properly
            if (opt.isPresent()) entity = opt.get();

            // setup entity
            if (entity instanceof Projectile pr) {
                pr.cachedOwner = null;
                pr.ownerUUID = null;
                pr.setOwner(owner);

                pr.shoot(facing.x, facing.y, facing.z,
                        scalePower , inaccuracy);
            }

            //  float radius = entity.getBbWidth() * 1.42f;
            //firePos = firePos.add(facing.normalize().scale(radius));
            entity.setPos(firePos.x, firePos.y, firePos.z);

            level.addFreshEntity(entity);

            return true;
        }
        return false;
    }

    private static final GameProfile FAKE_PLAYER = new GameProfile(UUID.fromString("11242C44-14d5-1f22-3d27-13D2C45CA355"), "[CANNON_TESTER]");

    // facing is likely not needed
    @Nullable
    protected Entity createEntity(ItemStack projectile, ProjectileTestLevel testLevel, Vec3 facing) {

        // fake player living in fake level
        Player fakePlayer = FakePlayerManager.get(FAKE_PLAYER, testLevel);

        fakePlayer.setXRot((float) MthUtils.getPitch(facing));
        fakePlayer.setYRot((float) MthUtils.getYaw(facing));

        testLevel.setup();

        if (projectile.getItem() instanceof ArrowItem ai) {
            return ai.createArrow(testLevel, projectile, fakePlayer);
        }
        //create from item

        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, projectile.copy());

        var eventResult = SuppPlatformStuff.fireItemUseEvent(fakePlayer, InteractionHand.MAIN_HAND);
        if(!eventResult.getResult().consumesAction()) {
            projectile.use(testLevel, fakePlayer, InteractionHand.MAIN_HAND);
        }

        return testLevel.projectile;
    }



}
