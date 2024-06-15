package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.core.misc.DummyWorld;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GenericProjectileBehavior implements IFireItemBehavior, IBallistic {

    @Override
    public Data calculateData(ItemStack projectile, Level level) {
        if (projectile.isEmpty()) {
            return IBallistic.LINE;
        }
        Entity proj = createEntity(projectile, level, Vec3.ZERO);
        if (proj != null) {
            proj.setDeltaMovement(1, 0, 0);
            proj.tick();
            var newMovement = proj.getDeltaMovement();
            float drag = (float) newMovement.x;
            float gravity = (float) -newMovement.y;
            return new Data(drag, gravity);
        }
        return IBallistic.LINE;
    }

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos,
                        Vec3 facing, float power, float drag, int inaccuracy, @Nullable Player owner) {
        Entity entity = createEntity(stack, level, facing);

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

                pr.shoot(facing.x, facing.y, facing.z, drag * power, inaccuracy);
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

    @Nullable
    protected Entity createEntity(ItemStack projectile, Level level, Vec3 facing) {
        //we could hae subclassed here...
        ProjectileTestLevel testLevel = ProjectileTestLevel.getCachedInstance("cannon_test_level", ProjectileTestLevel::new);

        if (projectile.is(Items.FIRE_CHARGE)) return EntityType.SMALL_FIREBALL.create(testLevel);
        if (projectile.is(ModRegistry.CANNONBALL_ITEM.get())) return ModEntities.CANNONBALL.get().create(testLevel);

        Player fakePlayer = FakePlayerManager.get(FAKE_PLAYER, level);
        fakePlayer.setXRot((float) getPitch(facing));
        fakePlayer.setYRot((float) getYaw(facing));

        testLevel.setup();

        if (projectile.getItem() instanceof ArrowItem ai) {
            return ai.createArrow(testLevel, projectile, fakePlayer);
        }
        //create from item


        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, projectile.copy());
        projectile.use(testLevel, fakePlayer, InteractionHand.MAIN_HAND);

        return testLevel.projectile;
    }

    private static class ProjectileTestLevel extends DummyWorld {

        @Nullable
        private Entity projectile = null;

        public ProjectileTestLevel() {
            super(false, false);
        }

        public void setup() {
            projectile = null;
        }

        @Override
        public boolean addFreshEntity(Entity entity) {
            this.projectile = entity;
            return true;
        }
    }

    @Deprecated(forRemoval = true)
    //in degrees
    public static double getPitch(Vec3 vec3) {
        return -Math.toDegrees(Math.asin(vec3.y));
    }

    @Deprecated(forRemoval = true)
    // in degrees
    public static double getYaw(Vec3 vec3) {
        return Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
    }

}
