package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundExplosionPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CannonBallEntity extends ImprovedProjectileEntity {

    private boolean removeNextTick;

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
        if (this.removeNextTick) {
            this.discard();
        }
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
                // random circular vector
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
        super.onHitBlock(result);
        if (this.getDeltaMovement().length() < 0.4) {
            this.removeNextTick = true;
        }

        if (!level().isClientSide) {
            float radius = 1.1f;

            Vec3 movement = this.getDeltaMovement();
            double vel = Math.abs(movement.length());

            // this derives from kinetic energy calculation
            float scaling = 40f;
            float maxAmount = (float) (vel * vel * scaling);

            Vec3 loc = result.getLocation();

            BlockPos pos = result.getBlockPos();
            CannonBallExplosion exp = new CannonBallExplosion(this.level(), this,
                    loc.x(), loc.y(), loc.z(), pos, maxAmount, radius);
            exp.explode();
            exp.finalizeExplosion(true);


            float exploded = exp.getExploded();

            double speedUsed = exploded / maxAmount;
            this.setDeltaMovement(movement.normalize().scale(1 - speedUsed));
            Message message = ClientBoundExplosionPacket.cannonball(loc, radius, exp.getToBlow(), this);

            ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(this.level(), pos, message);
        }
    }

}
