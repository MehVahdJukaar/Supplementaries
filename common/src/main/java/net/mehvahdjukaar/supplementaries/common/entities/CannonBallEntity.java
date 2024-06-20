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
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CannonBallEntity extends ImprovedProjectileEntity {

    //for collisions we should ignored since they were handled by the other entity
    private final List<CannonBallEntity> justCollidedWith = new ArrayList<>();

    public CannonBallEntity(LivingEntity thrower) {
        super(ModEntities.CANNONBALL.get(), thrower, thrower.level());
        this.maxAge = 300;
        this.blocksBuilding = true;
    }

    public CannonBallEntity(EntityType<CannonBallEntity> type, Level level) {
        super(type, level);
        this.maxAge = 300;
        this.blocksBuilding = true;
    }

    @Override
    protected Item getDefaultItem() {
        return ModRegistry.CANNONBALL.get().asItem();
    }

    @Override
    public void tick() {
        super.tick();
        justCollidedWith.clear();
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

            if (maybeBounce(result.getBlockPos(), result.getDirection())) {
                return;
            }


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

            if (exploded != 0) {
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

    private boolean maybeBounce(BlockPos pos, Direction hitDirection) {
        boolean shouldBounce;
        Vec3 velocity = this.getDeltaMovement();
        Vector3f surfaceNormal = hitDirection.step();

        BlockState hitBlock = level().getBlockState(pos);
        if (hitBlock.getBlock() instanceof SlimeBlock) {
            shouldBounce = true;
        } else {
            double dot = surfaceNormal.dot(velocity.toVector3f());

            double cosAngle = Math.abs(dot / velocity.length());

            float bounceCosAngle = Mth.cos(75 * Mth.DEG_TO_RAD);
            shouldBounce = cosAngle < bounceCosAngle;
        }

        if (shouldBounce) {
            Vec3 newVel = new Vec3(velocity.toVector3f().reflect(surfaceNormal));
            this.setDeltaMovement(newVel);
            SoundType soundType = hitBlock.getSoundType();
            this.playSound(soundType.getFallSound(), soundType.volume*1.5f, soundType.getPitch());
            return true;
        }
        return false;

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();

        // elastic collision with some loss
        float lossFactor = target instanceof LivingEntity le ? 0.1f : 0.8f;
        float cannonballDensity = 10f; //denser than other entities
        float m2 = (float) target.getBoundingBox().getSize() * (target instanceof CannonBallEntity ? cannonballDensity : 0);
        float m1 = (float) this.getBoundingBox().getSize() * cannonballDensity;
        Vector3f v2i = target.getDeltaMovement().toVector3f();
        Vector3f v1i = this.getDeltaMovement().toVector3f();

        double initialKineticEnergy = 0.5f * m1 * v1i.lengthSquared() + 0.5f * m2 * v2i.lengthSquared();

        // smaller means more losses
        float elasticity = 1;
        if (target instanceof LivingEntity le) {
            double lostEnergy = initialKineticEnergy * (1 - lossFactor);
            float dmgMult = 4;
            if (le.hurt(ModDamageSources.cannonBallExplosion(this, this.getOwner()), (float) lostEnergy * dmgMult)) {
                elasticity = Mth.sqrt(1 - lossFactor);
            }
        }

        Vector3f v1f = new Vector3f();
        Vector3f v2f = new Vector3f();

        // Calculate final velocities in each dimension. has conservation of momentum and kinetic energy
        for (int i = 0; i < 3; i++) {
            float c1 = (v1i.get(i) * (m1 - m2) + elasticity * 2 * m2 * v2i.get(i)) / (m1 + m2);
            float c2 = (v2i.get(i) * (m2 - m1) + elasticity * 2 * m1 * v1i.get(i)) / (m1 + m2);
            v1f.setComponent(i, c1);
            v2f.setComponent(i, c2);
        }

        // Apply new velocities
        this.setDeltaMovement(new Vec3(v1f));
        target.setDeltaMovement(new Vec3(v2f));

        // Calculate final momentum
        Vector3f finalMomentum = v1f.mul(m1, new Vector3f()).add(v2f.mul(m2, new Vector3f()));


        // Calculate final kinetic energy
        double finalKineticEnergy = 0.5f * m1 * v1f.lengthSquared() + 0.5f * m2 * v2f.lengthSquared();

        if (target instanceof CannonBallEntity c) {
            c.justCollidedWith.add(this);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof CannonBallEntity c) {
            return !c.justCollidedWith.contains(this);
        }
        return super.canHitEntity(target);
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        //solid! needed so collisions wont be called multiple times
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean collidesWithBlocks() {
        return true;
    }
}
