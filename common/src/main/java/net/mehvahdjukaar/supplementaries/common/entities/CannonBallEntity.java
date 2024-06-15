package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CannonBallEntity extends ImprovedProjectileEntity {

    public CannonBallEntity(Level world, Player playerIn) {
        super(ModEntities.CANNONBALL.get(), playerIn, world);
    }

    public CannonBallEntity(EntityType<CannonBallEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModRegistry.CANNONBALL.get().asItem();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void spawnTrailParticles() {
        var speed = this.getDeltaMovement();
        var normalSpeed = speed.normalize();
        // Calculate pitch and yaw in radians
        double pitch = Math.asin(normalSpeed.y);
        double yaw = Math.atan2(normalSpeed.x, normalSpeed.z);

        double dx = getX() - xo;
        double dy = getY() - yo;
        double dz = getZ() - zo;


        for (int k = 0; k < 2; k++) {
            if (random.nextFloat() < speed.length() * 0.35) {

                Vector3f offset = new Vector3f(0, (random.nextFloat() * this.getBbWidth() * 0.7f), 0);
                offset.rotateZ(level().random.nextFloat() * Mth.TWO_PI);

                // Apply rotations
                offset.rotateX((float) pitch);
                offset.rotateY((float) yaw);
                float j = random.nextFloat() * -0.5f;

                this.level().addParticle(ModParticles.WIND_STREAM.get(),
                        offset.x + j * dx,
                        offset.y + j * dy + this.getBbWidth() / 3,
                        offset.z + j * dz,
                        this.getId(), 0, 0
                );
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        float radius = 1.5f;

        var loc = result.getLocation();
        CannonBallExplosion exp = new CannonBallExplosion(this.level(), this,
                loc.x(), loc.y(), loc.z(), result.getBlockPos(), radius);
        exp.explode();
        exp.finalizeExplosion(true);

        Vec3 speed = this.getDeltaMovement();
        double len = Math.abs(speed.length());
        double decrement = 0.3;
        double newLen = Math.max(0, len - decrement);
        this.setDeltaMovement(speed.normalize().scale(newLen));
        super.onHitBlock(result);

        if (newLen < decrement) {
            this.discard();
        }
        //this.discard();
    }

}
