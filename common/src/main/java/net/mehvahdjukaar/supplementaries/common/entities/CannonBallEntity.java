package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundExplosionPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.mixins.LivingEntityAccessor;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CannonBallEntity extends ImprovedProjectileEntity {

    //for collisions that we should ignore since they were handled by the other entity
    private final List<CannonBallEntity> justCollidedWith = new ArrayList<>();
    private int bounces = 0; //bounces

    public CannonBallEntity(LivingEntity thrower) {
        super(ModEntities.CANNONBALL.get(), thrower, thrower.level());
        this.maxAge = 6000;
        this.blocksBuilding = true;
    }

    public CannonBallEntity(EntityType<CannonBallEntity> type, Level level) {
        super(type, level);
        this.maxAge = 6000;

        this.blocksBuilding = true;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("bounces", this.bounces);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.bounces = compound.getInt("bounces");
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
    protected float getGravity() {
        return ProjectileStats.CANNONBALL_GRAVITY;
    }

    @Override
    public float getDefaultShootVelocity() {
        return ProjectileStats.CANNONBALL_SPEED;
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
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        var particleData = ClientConfigs.Items.CANNONBALL_3D.get() ?
                new BlockParticleOption(ParticleTypes.BLOCK, ModRegistry.CANNONBALL.get().defaultBlockState()) :
                new ItemParticleOption(ParticleTypes.ITEM, this.getItem());

        //TODO use block particles with config, spawn more and maybe use send particles insteaed
        if (id == 3) {
            float speed = 0.05f;
            for (int i = 0; i < 8; ++i) {
                double k = this.random.nextGaussian() * speed;
                double l = this.random.nextGaussian() * speed;
                double m = this.random.nextGaussian() * speed;
                this.level().addParticle(particleData,
                        this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
                        k, l, m);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (maybeBounce(result)) {
            return;
        }

        if (!level().isClientSide) {
            float radius = 1.1f;

            Vec3 movement = this.getDeltaMovement();
            double vel = Math.abs(movement.length());

            // this derives from kinetic energy calculation
            float scaling = 5;
            float maxAmount = (float) (vel * vel * scaling);

            //centered on cannonball so we always get rid of all blocks around it so we can move freely next tick
            Vec3 loc = this.position();

            BlockPos pos = result.getBlockPos();
            CannonBallExplosion exp = new CannonBallExplosion(this.level(), this,
                    loc.x(), loc.y(), loc.z(), pos, maxAmount, radius);
            exp.explode();
            exp.finalizeExplosion(true);


            float exploded = exp.getExploded();

            if (exploded != 0) {
                double speedUsed = exploded / maxAmount;
                double factor = 1 - speedUsed;
                if (factor <= 0 || factor > 1) {
                    Supplementaries.error();
                }
                this.setDeltaMovement(movement.scale(factor));
                Message message = ClientBoundExplosionPacket.cannonball(exp, this);

                NetworkHelper.sendToAllClientPlayersInDefaultRange(this.level(), pos, message);
            }
            this.hasImpulse = true;

            if (this.getDeltaMovement().lengthSqr() < (0.2 * 0.2) || exploded == 0) {
                this.playSound(ModSounds.CANNONBALL_BREAK.get(), 1.0F, 1.5F);
                this.level().broadcastEntityEvent(this, (byte) 3);
                this.discard();
            } else {
                // advance until we go as far as we would have gone had we not had any collisions
                Vec3 targetPos = new Vec3(xo, yo, zo).add(this.movementOld);
                Vec3 missingMovement = targetPos.subtract(this.position());
                //this is a recursive call!
                if (missingMovement.lengthSqr() > 0.01 * 0.01) {
                    this.move(MoverType.SELF, missingMovement);
                }
            }
        }
    }

    private boolean maybeBounce(BlockHitResult hit) {
        if (bounces >= 3) return false;
        bounces++;

        Direction hitDirection = hit.getDirection();
        BlockPos pos = hit.getBlockPos();

        boolean shouldBounce;
        Vec3 velocity = this.getDeltaMovement();
        Vector3f surfaceNormal = hitDirection.step();

        Level level = level();
        BlockState hitBlock = level.getBlockState(pos);
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
            this.hasImpulse = true;

            double missingDistance = velocity.subtract(this.position().subtract(new Vec3(xo, yo, zo))).length();
            this.setPos(this.position().add(newVel.normalize().scale(missingDistance)));

            level.gameEvent(GameEvent.HIT_GROUND, this.position(), GameEvent.Context.of(this, hitBlock));
            this.addLandingEffects(hit);

            return true;
        }
        return false;

    }

    private void addLandingEffects(BlockHitResult hit) {
        this.playSound(ModSounds.CANNONBALL_BOUNCE.get(), 1 * 2.2f, 1);

        BlockPos pos = hit.getBlockPos();
        BlockState state = level().getBlockState(pos);

        double speed = this.getDeltaMovement().lengthSqr();
        double minSpeed = 0.2f;
        if (!this.level().isClientSide && !state.isAir() && speed > minSpeed) {
            double x = hit.getLocation().x;
            double y = hit.getLocation().y;
            double z = hit.getLocation().z;

            int count = Math.min(10, (int) (speed * 4) + 1);

            BlockParticleOption blockParticleOption = new BlockParticleOption(ParticleTypes.BLOCK, state);
            SuppPlatformStuff.setParticlePos(blockParticleOption, pos);
            ((ServerLevel) this.level()).sendParticles(blockParticleOption,
                    x, y, z, count, 0.0, 0.0, 0.0, 0.15);

        }

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();

        // elastic collision with some loss
        float lossFactor = target instanceof LivingEntity le ? 0.1f : 0.8f;
        float cannonballDensity = 8f; //denser than other entities
        float m2 = (float) target.getBoundingBox().getSize() * (target instanceof CannonBallEntity ? cannonballDensity : 0);
        float m1 = (float) this.getBoundingBox().getSize() * cannonballDensity;
        Vector3f v2i = target.getDeltaMovement().toVector3f();
        Vector3f v1i = this.getDeltaMovement().toVector3f();

        double initialKineticEnergy = 0.5f * m1 * v1i.lengthSquared() + 0.5f * m2 * v2i.lengthSquared();

        // smaller means more losses
        float elasticity = 1;
        if (target instanceof LivingEntity le) {
            double lostEnergy = initialKineticEnergy * (1 - lossFactor);
            float dmgMult = 3.5f; //TODO: config
            float amount = (float) lostEnergy * dmgMult;
            float oldHealth = le.getHealth();
            if (le.hurt(ModDamageSources.cannonBallExplosion(this, this.getOwner()), amount)) {
                elasticity = Mth.sqrt(1 - lossFactor);
            }
            if (!level().isClientSide && le instanceof Creeper && oldHealth >= le.getMaxHealth()) {
                maybeDropDisc(le);
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
        this.hasImpulse = true;

        // Calculate final momentum
        Vector3f finalMomentum = v1f.mul(m1, new Vector3f()).add(v2f.mul(m2, new Vector3f()));

        // Calculate final kinetic energy
        double finalKineticEnergy = 0.5f * m1 * v1f.lengthSquared() + 0.5f * m2 * v2f.lengthSquared();
        if (target instanceof CannonBallEntity c) {
            c.justCollidedWith.add(this);
            this.playSound(ModSounds.CANNONBALL_BOUNCE.get(), 1 * 2.2f, 1);
        }
    }

    private void maybeDropDisc(LivingEntity le) {
        if (!le.isAlive() && CommonConfigs.Functional.AVAST_DISC_ENABLED.get() && this.getOwner() instanceof Player) {
            if (((LivingEntityAccessor) le).invokeShouldDropLoot() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                le.spawnAtLocation(ModRegistry.AVAST_DISC.get());
                // we cant use global loot modifiers because we dont have access to damage value there
            }
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
        return false;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {

        //TODO: kill on fire damage
        return super.hurt(source, amount);
    }
}
