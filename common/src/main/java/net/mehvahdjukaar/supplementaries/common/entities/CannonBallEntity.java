package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CannonBallEntity extends ImprovedProjectileEntity {

    public CannonBallEntity(Level world, Player playerIn) {
        super(ModEntities.CANNONBALL.get(),playerIn, world);
    }

    public CannonBallEntity(EntityType<CannonBallEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModRegistry.CANNONBALL.get().asItem();
    }

    @Override
    protected float getGravity() {
        return super.getGravity();
    }

    @Override
    protected float getDeceleration() {
        return super.getDeceleration();
    }

    @Override
    public void spawnTrailParticles(Vec3 currentPos, Vec3 newPos) {
        super.spawnTrailParticles(currentPos, newPos);
        double x = currentPos.x;
        double y = currentPos.y;
        double z = currentPos.z;
        double dx = newPos.x - x;
        double dy = newPos.y - y;
        double dz = newPos.z - z;
        int s = 4;
        var speed = this.getDeltaMovement();
        for (int i = 0; i < s; ++i) {
            double j = i / (double) s;
            this.level().addParticle(ModParticles.WIND_STREAM.get(),
                    x - dx * j + random.nextGaussian() * 0.1,
                    y - dy * j + random.nextGaussian() * 0.1,
                    z - dz * j + random.nextGaussian() * 0.1,
                    speed.x, speed.y, speed.z);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        float radius = 4;

        Explosion exp = level().explode(this, getX(), getY(), getZ(), radius, Level.ExplosionInteraction.TNT);
        //exp.finalizeExplosion();
        Vec3 speed = this.getDeltaMovement();
        this.setDeltaMovement(speed.scale(0.4));
        super.onHitBlock(result);

        if(this.getDeltaMovement().lengthSqr()<0.2){
            this.discard();
        }
    }
}
