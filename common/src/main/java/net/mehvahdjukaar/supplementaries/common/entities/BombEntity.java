package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.IExtraClientSpawnData;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.BombExplosion;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BombEntity extends ImprovedProjectileEntity implements IExtraClientSpawnData {

    // overrides in the bomb item models: the lit fuse animation while flying, and the smaller shard sprite
    public static final int PROJECTILE_MODEL_DATA = 1;
    public static final int BABY_MODEL_DATA = 2;
    // speed the baby bombs of a blue bomb are flung out at. Tuned so their arc lasts long enough
    // to act as a fuse and lands them roughly 2 to 4 blocks out
    private static final float CLUSTER_SPEED = 0.4f;
    private static final float CLUSTER_INACCURACY = 1.5f;
    private static final int WEAKNESS_DURATION = 20 * 30;

    private final boolean hasFuse = CommonConfigs.Tools.BOMB_FUSE.get() != 0;
    private final ParticleTrailEmitter trailEmitter = ParticleTrailEmitter.builder()
            .spacing(0.25)
            .maxParticlesPerTick(20)
            .minParticlesPerTick(1)
            .build();
    private BombType type = BombType.NORMAL;
    private boolean active = true;
    // ticks left before going off. -1 means it hasn't hit anything yet
    private int fuseTimer = -1;
    private boolean superCharged = false;
    // face of the block we landed on. Baby bombs are flung along it so they end up in open space
    private Direction hitFace = Direction.UP;


    public BombEntity(EntityType<? extends BombEntity> type, Level world) {
        super(type, world);
        this.maxAge = (hasFuse ? CommonConfigs.Tools.BOMB_FUSE.get() : 200);
    }

    public BombEntity(Level worldIn, LivingEntity throwerIn, BombType type) {
        super(ModEntities.BOMB.get(), throwerIn, worldIn);
        this.setType(type);
        this.maxAge = (hasFuse ? CommonConfigs.Tools.BOMB_FUSE.get() : 300);
    }

    public BombEntity(Level worldIn, double x, double y, double z, BombType type) {
        super(ModEntities.BOMB.get(), x, y, z, worldIn);
        this.setType(type);
        this.maxAge = (hasFuse ? CommonConfigs.Tools.BOMB_FUSE.get() : 300);
    }

    public BombType getBombType() {
        return this.type;
    }

    // the item is what actually gets rendered, and it's defaulted during the super constructor,
    // before we know our type. Has to be re-set here or every bomb flies looking like a normal one
    private void setType(BombType type) {
        this.type = type;
        this.setItem(type.createDisplayStack());
    }

    //data to be saved when the entity gets unloaded
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Active", this.active);
        compound.putInt("Type", this.type.ordinal());
        compound.putInt("Timer", this.fuseTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.active = compound.getBoolean("Active");
        this.type = BombType.values()[compound.getInt("Type")];
        this.fuseTimer = compound.getInt("Timer");
    }

    //this is extra data needed when an entity creation packet is sent from server to client
    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        this.type = buffer.readEnum(BombType.class);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(this.type);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return PlatHelper.getEntitySpawnPacket(this, serverEntity);
    }

    @Override
    protected Item getDefaultItem() {
        // called from defineSynchedData while the super constructor runs, before type is assigned
        return this.type == null ? ModRegistry.BOMB_ITEM.get() : this.type.getItem();
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
        return super.hasReachedEndOfLife() || this.fuseTimer == 0;
    }

    @Override
    public void tick() {
        if (this.active && this.isInWater() && !this.type.isWaterProof()) {
            this.turnOff();
        }
        if (this.fuseTimer > 0) {
            this.fuseTimer--;
        }
        super.tick();
    }


    @Override
    public void spawnTrailParticles() {
        trailEmitter.tick(this, (p, v) -> {
            this.level().addParticle(ParticleTypes.SMOKE,
                    p.x,
                    0.25 + p.y,
                    p.z,
                    0, 0.02, 0);
        });

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
            // hand back a plain item, not the in-flight display stack with its model override
            if (!this.active && entityIn.getInventory().add(new ItemStack(this.type.getItem()))) {
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
            this.hitFace = hit.getDirection();
            this.setDeltaMovement(Vec3.ZERO);
            this.setOnGround(true);
            activateBomb();
            //TODO: Fix
        }
    }

    private void activateBomb() {
        Level level = level();
        if (level.isClientSide || this.hasFuse) return;

        if (this.fuseTimer == -1) {
            this.fuseTimer = this.superCharged ? 0 : this.type.impactFuse(level.getRandom());
        }
        if (this.fuseTimer == 0 && !this.isRemoved()) {
            this.reachedEndOfLife();
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
    public double getDefaultGravity() {
        return ProjectileStats.BOMB_GRAVITY;
    }

    //createMiniExplosion
    @Override
    public void reachedEndOfLife() {
        Level level = level();
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_BREAK, SoundSource.NEUTRAL,
                this.type.clankVolume(), this.type.clankPitch());

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

        int cluster = this.type.splitCount();
        // a supercharged bomb that splits pays out in extra shards instead of a second big blast
        if (this.superCharged) {
            if (cluster > 0) {
                cluster *= 2;
            } else {
                //second explosion when supercharged
                //TODO: check explosion mode
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 6f,
                        breaks, this.getOwner() instanceof Player ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.MOB);
            }
        }
        if (level() instanceof ServerLevel sl) {
            Vec3 center = new Vec3(this.getX(), this.getY() + 0.25, this.getZ());
            BombExplosion.createExplosion(this, sl, center.x, center.y, center.z, this.type, breaks);
            this.applyBlastEffects(sl, center);
            if (cluster > 0) {
                this.spawnCluster(sl, center, cluster);
            }
        }
    }

    private void applyBlastEffects(ServerLevel level, Vec3 center) {
        double radius = this.type.getRadius();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(center, radius * 2, radius * 2, radius * 2))) {
            if (e.distanceToSqr(center) <= radius * radius) {
                this.type.applyStatusEffects(e);
            }
        }
    }

    /**
     * Throws a ring of baby bombs out along the face we hit, so a floor blast rains them upward and a
     * ceiling blast rains them down instead of burying half the cluster in the block we just landed on.
     */
    private void spawnCluster(ServerLevel level, Vec3 center, int count) {
        Vec3 normal = Vec3.atLowerCornerOf(this.hitFace.getNormal());
        Vec3 helper = Math.abs(normal.y) > 0.5 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = normal.cross(helper).normalize();
        Vec3 up = normal.cross(right);

        RandomSource random = level.getRandom();
        // the whole ring is rotated so two bombs in the same spot don't scatter along the same lines
        double ringOffset = random.nextDouble();
        for (int i = 0; i < count; i++) {
            // evenly spaced slots with a bit of wobble, so they stay spread instead of bunching up
            double yaw = (ringOffset + (i + Mth.lerp(random.nextDouble(), -0.3, 0.3)) / count) * Math.PI * 2;
            // how far each shard tips away from the surface: low values graze it, high ones pop straight out
            double lift = Mth.lerp(random.nextDouble(), 0.55, 0.9);
            double spread = Math.sqrt(1 - lift * lift);
            Vec3 dir = right.scale(Math.cos(yaw) * spread)
                    .add(up.scale(Math.sin(yaw) * spread))
                    .add(normal.scale(lift));

            Vec3 spawnPos = center.add(dir.scale(0.4));
            BombEntity baby = new BombEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, BombType.BLUE_BABY);
            baby.setOwner(this.getOwner());
            baby.shoot(dir.x, dir.y, dir.z, CLUSTER_SPEED, CLUSTER_INACCURACY);
            level.addFreshEntity(baby);
        }
    }

    public enum BreakingMode {
        ALL,
        WEAK,
        NONE
    }

    public enum BombType {
        NORMAL,
        BLUE,
        // the shards a blue bomb scatters. Never thrown by hand, only spawned by a detonating BLUE
        BLUE_BABY;

        public double getRadius() {
            return switch (this) {
                case BLUE -> CommonConfigs.Tools.BOMB_BLUE_RADIUS.get();
                case BLUE_BABY -> CommonConfigs.Tools.BOMB_BLUE_BABY_RADIUS.get();
                default -> CommonConfigs.Tools.BOMB_RADIUS.get();
            };
        }

        public BreakingMode breakMode() {
            return isBlue() ? CommonConfigs.Tools.BOMB_BLUE_BREAKS.get() : CommonConfigs.Tools.BOMB_BREAKS.get();
        }

        public boolean isBlue() {
            return this == BLUE || this == BLUE_BABY;
        }

        // blue powder keeps burning underwater, and so do its shards
        public boolean isWaterProof() {
            return isBlue();
        }

        public Item getItem() {
            return isBlue() ? ModRegistry.BOMB_BLUE_ITEM.get() : ModRegistry.BOMB_ITEM.get();
        }

        /**
         * What a flying bomb renders as: the animated lit fuse sprite, or the smaller shard one for shards.
         */
        public ItemStack createDisplayStack() {
            ItemStack stack = new ItemStack(getItem());
            stack.set(DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(this == BLUE_BABY ? BABY_MODEL_DATA : PROJECTILE_MODEL_DATA));
            return stack;
        }

        public int splitCount() {
            return this == BLUE ? CommonConfigs.Tools.BOMB_BLUE_SPLIT_COUNT.get() : 0;
        }

        /**
         * Ticks between hitting something and going off. Shards get a random one so a cluster that lands
         * all at once still crackles through instead of merging into a single blast.
         */
        public int impactFuse(RandomSource random) {
            return this == BLUE_BABY ? 3 + random.nextInt(8) : 0;
        }

        public void applyStatusEffects(LivingEntity entity) {
            switch (this) {
                case BLUE -> entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION));
                case BLUE_BABY -> {
                    int fireSeconds = CommonConfigs.Tools.BOMB_BLUE_BABY_FIRE.get();
                    if (fireSeconds > 0) entity.igniteForSeconds(fireSeconds);
                }
                default -> {
                }
            }
        }

        // five shards firing full blast sounds on top of each other is a wall of noise
        public float explosionVolume() {
            return this == BLUE_BABY ? 1.4f : 4f;
        }

        public float explosionPitch(RandomSource random) {
            float base = (1 + (random.nextFloat() - random.nextFloat()) * 0.2f) * 0.7f;
            return this == BLUE_BABY ? base * 1.8f : base;
        }

        public float clankVolume() {
            return this == BLUE_BABY ? 0.5f : 1.5f;
        }

        public float clankPitch() {
            return this == BLUE_BABY ? 1.9f : 1.5f;
        }
    }


}
