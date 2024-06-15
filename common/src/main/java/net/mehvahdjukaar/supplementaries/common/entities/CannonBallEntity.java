package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundExplosionPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CannonBallEntity extends ImprovedProjectileEntity {

    public CannonBallEntity(Level world, Player playerIn) {
        super(ModEntities.CANNONBALL.get(), playerIn, world);
        this.maxAge = 300;
    }

    public CannonBallEntity(EntityType<CannonBallEntity> type, Level level) {
        super(type, level);
        this.maxAge = 300;
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

    private void playDestroyEffects() {
        for (int i = 0; i < 8; ++i) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
        this.playSound(SoundEvents.METAL_BREAK, 1.0F, 1.5F);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide) {
            float radius = 1.1f;

            Vec3 movement = this.getDeltaMovement();
            double vel = Math.abs(movement.length());

            // this derives from kinetic energy calculation
            float scaling = 30;
            float maxAmount = (float) (vel * vel * scaling);

            Vec3 loc = result.getLocation();

            BlockPos pos = result.getBlockPos();
            CannonBallExplosion exp = new CannonBallExplosion(this.level(), this,
                    loc.x(), loc.y(), loc.z(), pos, maxAmount, radius);
            exp.explode();
            exp.finalizeExplosion(true);


            float exploded = exp.getExploded();

            if(exploded != 0) {
                double speedUsed = exploded / maxAmount;
                this.setDeltaMovement(movement.normalize().scale(1 - speedUsed));
                Message message = ClientBoundExplosionPacket.cannonball(exp, this);

                ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(this.level(), pos, message);
            }

            if (this.getDeltaMovement().length() < 0.4 || exploded == 0) {
                playDestroyEffects();
                this.discard();
            }

        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        double speed = this.getDeltaMovement().length();
        float dmg = (float) (5 * speed * speed);
        result.getEntity().hurt(ModDamageSources.cannonBallExplosion(this, this.getOwner()), dmg);
    }
}
