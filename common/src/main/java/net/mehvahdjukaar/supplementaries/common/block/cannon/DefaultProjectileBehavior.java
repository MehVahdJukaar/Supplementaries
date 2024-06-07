package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.core.misc.DummyWorld;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.minecraft.core.BlockPos;
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

public class DefaultProjectileBehavior implements ICannonBehavior {

    private final float drag;
    private final float gravity;

    public DefaultProjectileBehavior(Level level, ItemStack projectile) {

        Entity proj = createEntity(projectile, level, Vec3.ZERO);

        proj.setDeltaMovement(1, 0, 0);
        proj.tick();
        var newMovement = proj.getDeltaMovement();
        this.drag = (float) newMovement.x;
        this.gravity = (float) -newMovement.y;
    }

    @Override
    public float getDrag() {
        return drag;
    }

    @Override
    public float getGravity() {
        return gravity;
    }

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, BlockPos pos, Vec3 facing, int power, float drag, int inaccuracy, @Nullable Player owner) {
        Entity entity = createEntity(stack, level, facing);

        // setup entity
        if (entity instanceof Projectile pr) {
            pr.cachedOwner = null;
            pr.ownerUUID = null;
            pr.setOwner(owner);

            pr.shoot(facing.x, facing.y, facing.z, -drag * power, inaccuracy);
        }

        entity.setPos(pos.getX() + 0.5 - facing.x,
                pos.getY() + 0.5 - facing.y, pos.getZ() + 0.5 - facing.z);


        level.addFreshEntity(entity);
        return true;
    }

    private static final GameProfile FAKE_PLAYER = new GameProfile(UUID.fromString("11242C44-14d5-1f22-3d27-13D2C45CA355"), "[CANNON_TESTER]");

    protected Entity createEntity(ItemStack projectile, Level level, Vec3 facing) {
        if (projectile.is(Items.FIRE_CHARGE)) return EntityType.SMALL_FIREBALL.create(level);

        Player fakePlayer = FakePlayerManager.get(FAKE_PLAYER, level);
        fakePlayer.setXRot((float) getPitch(facing));
        fakePlayer.setYRot((float) getYaw(facing));

        if (projectile.getItem() instanceof ArrowItem ai) {
            return ai.createArrow(level, projectile, fakePlayer);
        }

        //create from item

        ProjectileTestLevel testLevel = ProjectileTestLevel.getCachedInstance("cannon_test_level", ProjectileTestLevel::new);
        testLevel.setup();
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, projectile.copy());
        projectile.use(testLevel, fakePlayer, InteractionHand.MAIN_HAND);
        Entity entity = testLevel.projectile;

        //create new one just to be sure
        CompoundTag c = new CompoundTag();
        entity.save(c);
        var opt = EntityType.create(c, level); // create new to reset level properly

        return opt.orElseGet(() -> new SlingshotProjectileEntity(level, projectile, ItemStack.EMPTY));
    }

    private static class ProjectileTestLevel extends DummyWorld {

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
