package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.moonlight.api.entity.IExtraClientSpawnData;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeGenericPlayer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AwningBlock;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.BombExplosion;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundExplosionPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class BombEntity extends ImprovedProjectileEntity implements IExtraClientSpawnData {

    private final boolean hasFuse = CommonConfigs.Tools.BOMB_FUSE.get() != 0;
    private BombType type = BombType.NORMAL;
    private boolean active = true;
    private int changeTimer = -1;
    private boolean superCharged = false;

    public BombEntity(EntityType<? extends BombEntity> type, Level world) {
        super(type, world);
        this.maxAge = (hasFuse ? CommonConfigs.Tools.BOMB_FUSE.get() : 200);
    }

    public BombEntity(Level worldIn, LivingEntity throwerIn, BombType type) {
        super(ModEntities.BOMB.get(), throwerIn, worldIn);
        this.type = type;
        this.maxAge = (hasFuse ? CommonConfigs.Tools.BOMB_FUSE.get() : 300);
    }

    public BombEntity(Level worldIn, double x, double y, double z, BombType type) {
        super(ModEntities.BOMB.get(), x, y, z, worldIn);
        this.type = type;
        this.maxAge = (hasFuse ? CommonConfigs.Tools.BOMB_FUSE.get() : 300);
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
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return PlatHelper.getEntitySpawnPacket(this);
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
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, getItem()),
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 3 -> {
                spawnBreakParticles();
                this.discard();
            }
            case 10 -> {
                spawnBreakParticles();
                this.discard();
            }
            case 68 -> level().addParticle(ParticleTypes.SONIC_BOOM, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            case 67 -> {
                RandomSource random = level().getRandom();
                for (int i = 0; i < 10; ++i) {
                    level().addParticle(ParticleTypes.SMOKE, this.getX() + 0.25f - random.nextFloat() * 0.5f, this.getY() + 0.45f - random.nextFloat() * 0.5f, this.getZ() + 0.25f - random.nextFloat() * 0.5f, 0, 0.005, 0);
                }
                this.active = false;
            }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean hasReachedEndOfLife() {
        return super.hasReachedEndOfLife() || this.changeTimer == 0;
    }

    @Override
    public void tick() {
        if (this.active && this.isInWater() && this.type != BombType.BLUE) {
            this.turnOff();
        }
        super.tick();
    }


    @Override
    public void spawnTrailParticles() {
        Vec3 newPos = this.position();
        if (this.active && this.tickCount > 1) {

            double dx = newPos.x - xo;
            double dy = newPos.y - yo;
            double dz = newPos.z - zo;
            int s = 4;
            for (int i = 0; i < s; ++i) {
                double j = i / (double) s;
                this.level().addParticle(ParticleTypes.SMOKE,
                        xo - dx * j,
                        0.25 + yo - dy * j,
                        zo - dz * j,
                        0, 0.02, 0);
            }
        }
    }

    public void turnOff() {
        Level level = level();
        if (!level.isClientSide()) {
            level.broadcastEntityEvent(this, (byte) 67);
            this.playEntityOnFireExtinguishedSound();
        }
        this.active = false;
    }

    @Override
    public void playerTouch(Player entityIn) {
        if (!this.level().isClientSide) {
            if (!this.active && entityIn.getInventory().add(this.getItem())) {
                entityIn.take(this, 1);
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(level().damageSources().thrown(this, this.getOwner()), 1);
        if (hit.getEntity() instanceof LargeFireball) {
            this.superCharged = true;
            hit.getEntity().remove(RemovalReason.DISCARDED);
        }
        activateBomb();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);

        BlockState state = level().getBlockState(hit.getBlockPos());
        if (!state.is(ModTags.BOUNCY_BLOCKS) || hit.getDirection() != Direction.UP) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setOnGround(true);
            activateBomb();
            //TODO: Fix
        }
    }

    private void activateBomb() {
        Level level = level();
        if (!level.isClientSide && !this.hasFuse) {
            boolean isInstantlyActivated = this.type.isInstantlyActivated();
            if (!isInstantlyActivated && this.changeTimer == -1) {
                this.changeTimer = 5;
                //this.setDeltaMovement(Vector3d.ZERO);
                level.broadcastEntityEvent(this, (byte) 68);
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.NEUTRAL, 1.5f, 1.3f);
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

    @Override
    public float getDefaultShootVelocity() {
        return ProjectileStats.BOMB_SPEED;
    }

    @Override
    public float getGravity() {
        return ProjectileStats.BOMB_GRAVITY;
    }

    //createMiniExplosion
    @Override
    public void reachedEndOfLife() {
        Level level = level();
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_BREAK, SoundSource.NEUTRAL, 1.5F, 1.5f);

        if (!level.isClientSide) {
            if (this.active) {
                this.createExplosion();
                //spawn particles
                level.broadcastEntityEvent(this, (byte) 10);
            } else {
                level.broadcastEntityEvent(this, (byte) 3);
            }
            this.discard();
        }

        //client one is discarded when the event is recieved otherwise sometimes particles dont spawn
    }

    private void createExplosion() {

        boolean breaks = this.getOwner() instanceof Player ||
                PlatHelper.isMobGriefingOn(level(), this.getOwner());

        if (CompatHandler.FLAN && this.getOwner() instanceof Player p && !FlanCompat.canBreak(p, BlockPos.containing(position()))) {
            breaks = false;
        }

        if (this.superCharged) {
            //second explosion when supercharged
            //TODO: check explosion mode
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 6f, breaks, this.getOwner() instanceof Player ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.MOB);
        }

        BombExplosion explosion = new BombExplosion(this.level(), this,
                new BombExplosionDamageCalculator(this.type),
                this.getX(), this.getY() + 0.25, this.getZ(),
                this.type, breaks ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP);

        explosion.explode();
        explosion.finalizeExplosion(true);

        for (var p : level().players()) {
            if (p.distanceToSqr(this) < 64 * 64 && p instanceof ServerPlayer sp) {
                Message message = ClientBoundExplosionPacket.bomb(explosion, p);

                ModNetwork.CHANNEL.sendToClientPlayer(sp, message);
            }
        }

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

        public final Supplier<Item> item;
        public final Supplier<Item> itemOn;

        BombType(Supplier<Item> item, Supplier<Item> itemOn) {
            this.item = item;
            this.itemOn = itemOn;
        }

        public ItemStack getDisplayStack(boolean active) {
            return (active ? itemOn : item).get().getDefaultInstance();
        }

        public double getRadius() {
            return this == BLUE ? CommonConfigs.Tools.BOMB_BLUE_RADIUS.get() : CommonConfigs.Tools.BOMB_RADIUS.get();
        }

        public BreakingMode breakMode() {
            return this == BLUE ? CommonConfigs.Tools.BOMB_BLUE_BREAKS.get() : CommonConfigs.Tools.BOMB_BREAKS.get();
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
                }
            }
        }

        public boolean isInstantlyActivated() {
            return this != BLUE;
        }

        public void spawnExtraParticles(double x, double y, double z, Level level) {
            switch (this) {
                case BLUE -> {
                    //TODO: change all this
                    ParticleUtil.spawnParticleInASphere(level, x, y, z, () -> ParticleTypes.FLAME, 40, 0.4f,
                            0.01f, 0.15f);
                }
                case SPIKY -> {
                    RandomSource random = level.getRandom();
                    //maybe use method above?
                    var particle = CompatObjects.SHARPNEL.get();
                    if (particle instanceof ParticleOptions p) {
                        for (int i = 0; i < 80; i++) {
                            float dx = (float) (random.nextGaussian() * 2f);
                            float dy = (float) (random.nextGaussian() * 2f);
                            float dz = (float) (random.nextGaussian() * 2f);
                            level.addParticle(p, x, y, z, dx, dy, dz);
                        }
                    } else {
                        ParticleUtil.spawnParticleInASphere(level, x, y, z, () -> ParticleTypes.CRIT, 100, 5f,
                                0.01f, 0.15f);
                    }
                }
            }
        }

        public void afterExploded(BombExplosion exp, Level level) {
            if (this == SPIKY) {
                Vec3 pos = exp.getDamageSource().getSourcePosition();
                Entity e = exp.getIndirectSourceEntity();
                if (e == null) return;
                for (Entity entity : level.getEntities(e, new AABB(pos.x - 30, pos.y - 4, pos.z - 30,
                        pos.x + 30, pos.y + 4, pos.z + 30))) {
                    int random = (level.random.nextInt() * 100);
                    boolean shouldPoison = false;
                    if (entity.distanceToSqr(e) <= 4 * 4) {
                        shouldPoison = true;
                    } else if (entity.distanceToSqr(e) <= 8 * 8) {
                        if (random < 60) shouldPoison = true;
                    } else if (entity.distanceToSqr(e) <= 15 * 15) {
                        if (random < 30) shouldPoison = true;
                    } else if (entity.distanceToSqr(e) <= 30 * 30) {
                        if (random < 5) shouldPoison = true;
                    }
                    if (shouldPoison) {
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.hurt(level.damageSources().magic(), 2);
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (260 * 0.5f)));
                            var effect = CompatObjects.STUNNED_EFFECT.get();
                            if (effect != null) {
                                livingEntity.addEffect(new MobEffectInstance(effect, (int) (40 * 20 * 0.5f)));
                            }
                        }
                    }
                }
            }
        }
    }


    private static class BombExplosionDamageCalculator extends ExplosionDamageCalculator {
        private final BombType type;

        public BombExplosionDamageCalculator(BombType type) {
            this.type = type;
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
            return switch (type.breakMode()) {
                case ALL -> true;
                case WEAK -> state.canBeReplaced(Fluids.WATER) ||
                        state.is(ModTags.BOMB_BREAKABLE) ||
                        state.getBlock() instanceof TntBlock;
                default -> false;
            };
        }
    }


}
