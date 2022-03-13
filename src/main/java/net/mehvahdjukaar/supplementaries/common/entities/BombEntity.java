package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.mehvahdjukaar.supplementaries.common.world.explosion.BombExplosion;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.RegistryObject;

import java.util.Random;

public class BombEntity extends ImprovedProjectileEntity implements IEntityAdditionalSpawnData {

    private BombType type;
    private boolean active = true;
    private int changeTimer = -1;
    private boolean superCharged = false;

    public BombEntity(EntityType<? extends BombEntity> type, Level world) {
        super(type, world);
        this.maxAge = 200;
    }

    public BombEntity(Level worldIn, LivingEntity throwerIn, BombType type) {
        super(ModRegistry.BOMB.get(), throwerIn, worldIn);
        this.type = type;
        this.maxAge = 200;
    }

    public BombEntity(Level worldIn, double x, double y, double z, BombType type) {
        super(ModRegistry.BOMB.get(), x, y, z, worldIn);
        this.type = type;
        this.maxAge = 200;
    }

    public BombEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(ModRegistry.BOMB.get(), world);
        this.maxAge = 200;
        //packet.getAdditionalData().rea
    }

    //data to be saved when the entity gets unloaded
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Active", this.active);
        compound.putInt("Type", this.type.ordinal());
        compound.putInt("Timer", this.changeTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.active = compound.getBoolean("Active");
        this.type = BombType.values()[compound.getInt("Type")];
        this.changeTimer = compound.getInt("Timer");
    }

    //this is extra data needed when an entity creation packet is sent from server to client
    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.type = buffer.readEnum(BombType.class);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.type);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        return ModRegistry.BOMB_ITEM_ON.get();
    }

    @Override
    public ItemStack getItem() {
        return type.getDisplayStack(this.active);
    }

    private void spawnBreakParticles() {
        for (int i = 0; i < 8; ++i) {
            this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModRegistry.BOMB_ITEM.get())), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            default -> super.handleEntityEvent(id);
            case 3 -> spawnBreakParticles();
            case 10 -> {
                spawnBreakParticles();
                if (CommonUtil.FESTIVITY.isBirthday()) {
                    this.spawnParticleInASphere(ModRegistry.CONFETTI_PARTICLE.get(), 55, 0.3f);
                } else {
                    level.addParticle(ModRegistry.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), this.getX(), this.getY() + 1, this.getZ(),
                            this.type.getRadius(), 0, 0);
                }
                type.spawnExtraParticles(this);
            }
            case 68 -> level.addParticle(ParticleTypes.FLASH, this.getX(), this.getY() + 1, this.getZ(), 0, 0, 0);
            case 67 -> {
                Random random = level.getRandom();
                for (int i = 0; i < 10; ++i) {
                    level.addParticle(ParticleTypes.SMOKE, this.getX() + 0.25f - random.nextFloat() * 0.5f, this.getY() + 0.45f - random.nextFloat() * 0.5f, this.getZ() + 0.25f - random.nextFloat() * 0.5f, 0, 0.005, 0);
                }
                this.active = false;
            }
        }
    }

    private void spawnParticleInASphere(ParticleOptions type, int amount, float speed) {
        double d = (Math.PI * 2) / amount;
        for (float d22 = 0; d22 < (Math.PI * 2D); d22 += d) {
            Vec3 v = new Vec3(speed, 0, 0);
            v = v.yRot(d22 + random.nextFloat() * 0.3f);
            v = v.zRot((float) ((random.nextFloat()) * Math.PI));
            this.level.addParticle(type, this.getX(), this.getY() + 1, this.getZ(), v.x, v.y, v.z);
            //this.level.addParticle(ParticleTypes.SPIT, x, y, z, Math.cos(d22) * -10.0D, 0.0D, Math.sin(d22) * -10.0D);
        }
    }

    private double r() {
        return (random.nextGaussian()) * 0.05;
    }

    @Override
    public boolean hasReachedEndOfLife() {
        return super.hasReachedEndOfLife() || this.changeTimer == 0;
    }

    @Override
    public void tick() {

        if (this.changeTimer > 0) {
            this.changeTimer--;
            level.addParticle(ParticleTypes.SMOKE, this.position().x, this.position().y + 0.5, this.position().z, 0.0D, 0.0D, 0.0D);
        }

        if (this.active && this.isInWater() && this.type != BombType.BLUE) {
            this.turnOff();
        }

        super.tick();
    }

    @Override
    public void spawnTrailParticles(Vec3 currentPos, Vec3 newPos) {
        if (this.active && !this.firstTick) {

            double x = currentPos.x;
            double y = currentPos.y;
            double z = currentPos.z;
            double dx = newPos.x - x;
            double dy = newPos.y - y;
            double dz = newPos.z - z;
            int s = 4;
            for (int i = 0; i < s; ++i) {
                double j = i / (double) s;
                this.level.addParticle(ModRegistry.BOMB_SMOKE_PARTICLE.get(), x + dx * j, 0.5 + y + dy * j, z + dz * j, 0, 0.02, 0);
            }
        }
    }

    public static boolean canBreakBlock(BlockState state, BombType type) {
        return switch (type.breakMode()) {
            default -> false;
            case ALL -> true;
            case WEAK -> state.canBeReplaced(Fluids.WATER) ||
                    state.is(ModTags.BOMB_BREAKABLE) ||
                    state.getBlock() instanceof TntBlock;
        };
    }


    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 1);
        if (hit.getEntity() instanceof LargeFireball) {
            this.superCharged = true;
            hit.getEntity().remove(RemovalReason.DISCARDED);
        }
    }

    public void turnOff() {
        if (!level.isClientSide()) {
            this.level.broadcastEntityEvent(this, (byte) 67);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
        }
        this.active = false;
    }

    @Override
    public void playerTouch(Player entityIn) {
        if (!this.level.isClientSide) {
            if (!this.active && entityIn.getInventory().add(this.getItemStack())) {
                entityIn.take(this, 1);
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    private ItemStack getItemStack() {
        return new ItemStack(ModRegistry.BOMB_ITEM.get());
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        Vec3 vector3d = hit.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vector3d);
        Vec3 vector3d1 = vector3d.normalize().scale(getGravity());
        this.setPosRaw(this.getX() - vector3d1.x, this.getY() - vector3d1.y, this.getZ() - vector3d1.z);
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            boolean isInstantlyActivated = this.type.isInstantlyActivated();
            if (!isInstantlyActivated && this.changeTimer == -1) {
                this.changeTimer = 10;
                //this.setDeltaMovement(Vector3d.ZERO);
                this.level.broadcastEntityEvent(this, (byte) 68);
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.NEUTRAL, 1.5f, 1.3f);
            }

            //normal explosion
            if (!this.isRemoved()) {
                if (isInstantlyActivated || this.superCharged) {
                    this.reachedEndOfLife();
                }
            }
        }
    }

    @Override
    protected void updateRotation() {
    }

    //createMiniExplosion
    @Override
    public void reachedEndOfLife() {
        if (this.active) {
            this.createExplosion();
            //spawn particles
            this.level.broadcastEntityEvent(this, (byte) 10);
        } else {
            this.level.broadcastEntityEvent(this, (byte) 3);
        }
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_BREAK, SoundSource.NEUTRAL, 1.5F, 1.5f);
        this.remove(RemovalReason.DISCARDED);
    }

    private void createExplosion() {

        boolean breaks = this.getOwner() instanceof Player ||
                ForgeEventFactory.getMobGriefingEvent(this.level, this.getOwner());

        if (this.superCharged) {
            //second explosion when supercharged
            this.level.explode(this, this.getX(), this.getY(), this.getZ(), 6f, breaks, breaks ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
        }

        BombExplosion explosion = new BombExplosion(this.level, this, null, new ExplosionDamageCalculator() {
            public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
                return canBreakBlock(state, type);
            }
        },
                this.getX(), this.getY() + 0.25, this.getZ(), type.getRadius(),
                this.type, breaks ? Explosion.BlockInteraction.BREAK : Explosion.BlockInteraction.NONE);

        explosion.explode();
        explosion.doFinalizeExplosion();


    }

    public enum BreakingMode {
        ALL,
        WEAK,
        NONE
    }

    public enum BombType {
        NORMAL(ModRegistry.BOMB_ITEM, ModRegistry.BOMB_ITEM_ON),
        BLUE(ModRegistry.BOMB_BLUE_ITEM, ModRegistry.BOMB_BLUE_ITEM_ON),
        SPIKY(ModRegistry.BOMB_SPIKY_ITEM, ModRegistry.BOMB_SPIKY_ITEM_ON);

        public RegistryObject<Item> item;
        public RegistryObject<Item> item_on;

        BombType(RegistryObject<Item> item, RegistryObject<Item> item_on) {
            this.item = item;
            this.item_on = item_on;
        }

        public ItemStack getDisplayStack(boolean active) {
            return (active ? item_on : item).get().getDefaultInstance();
        }

        public float getRadius() {
            return this == BLUE ? ServerConfigs.cached.BOMB_BLUE_RADIUS : ServerConfigs.cached.BOMB_RADIUS;
        }

        public BreakingMode breakMode() {
            return this == BLUE ? ServerConfigs.cached.BOMB_BLUE_BREAKS : ServerConfigs.cached.BOMB_BREAKS;
        }

        public float volume() {
            return this == BLUE ? 5F : 3f;
        }

        public void applyStatusEffects(LivingEntity entity, double distSq) {
            switch (this) {
                case BLUE -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 30));
                    entity.setSecondsOnFire(10);
                }
                case SPIKY -> {

                    //we are using the explosion method since it has a bigger radius
                    /*
                    boolean shouldPoison = false;
                    float random = entity.getRandom().nextInt(100);
                    if (distSq <= 4 * 4) {
                        shouldPoison = true;
                    } else if (distSq <= 8 * 8) {
                        if (random < 60) shouldPoison = true;
                    } else if (distSq <= 15 * 15) {
                        if (random < 30) shouldPoison = true;
                    } else if (distSq <= 30 * 30) {
                        if (random < 5) shouldPoison = true;
                    }
                    if (shouldPoison) {
                        entity.hurt(DamageSource.MAGIC, 2);
                        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 260));
                        var effect = CompatObjects.STUNNED_EFFECT.get();
                        if (effect != null) {
                            entity.addEffect(new MobEffectInstance(effect, 40 * 20));
                        }
                    }
                    */
                }
            }
        }

        public boolean isInstantlyActivated() {
            return this != BLUE;
        }

        public void spawnExtraParticles(BombEntity bomb) {
            switch (this) {
                case BLUE -> {
                    bomb.spawnParticleInASphere(ParticleTypes.FLAME, 40, 0.6f);
                }
                case SPIKY -> {
                    //maybe use method above?
                    var particle = CompatObjects.SHARPNEL.get();
                    if (particle instanceof ParticleOptions p) {
                        for (int i = 0; i < 80; i++) {
                            float dx = (float) (bomb.random.nextGaussian() * 2f);
                            float dy = (float) (bomb.random.nextGaussian() * 2f);
                            float dz = (float) (bomb.random.nextGaussian() * 2f);
                            bomb.level.addParticle(p, bomb.getX(), bomb.getY() + 1, bomb.getZ(), dx, dy, dz);
                        }
                    }else{
                        bomb.spawnParticleInASphere(ParticleTypes.CRIT, 100, 5f);
                    }
                }
            }
        }

        public void afterExploded(BombExplosion exp, Level level) {
            if(this == SPIKY){
                Vec3 pos = exp.getPosition();
                Entity e = exp.getExploder();
                if(e==null)return;
                for (Entity entity :level.getEntities(e, new AABB(pos.x - 30, pos.y - 4, pos.z - 30,
                        pos.x + 30, pos.y + 4, pos.z + 30))){
                    int random = (int) (Math.random() * 100);
                    boolean shouldPoison = false;
                    if(entity.distanceToSqr(e) <= 4*4){
                        shouldPoison = true;
                    } else if (entity.distanceToSqr(e) <= 8*8) {
                        if(random < 60) shouldPoison = true;
                    } else if (entity.distanceToSqr(e) <= 15*15) {
                        if(random < 30) shouldPoison = true;
                    } else if (entity.distanceToSqr(e) <= 30*30) {
                        if(random < 5) shouldPoison = true;
                    }
                    if(shouldPoison){
                        if(entity instanceof LivingEntity livingEntity){
                            livingEntity.hurt(DamageSource.MAGIC, 2);
                            livingEntity.addEffect( new MobEffectInstance( MobEffects.POISON , (int) (260*0.5f)) );
                            var effect = CompatObjects.STUNNED_EFFECT.get();
                            if (effect != null) {
                                livingEntity.addEffect(new MobEffectInstance(effect, (int) (40 * 20*0.5f)));
                            }
                        }
                    }
                }
            }
        }
    }



}
