//package net.mehvahdjukaar.supplementaries.entities;
//
//import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
//import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.protocol.Packet;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.tags.BlockTags;
//import net.minecraft.tags.Tag;
//import net.minecraft.util.Mth;
//import net.minecraft.world.DifficultyInstance;
//import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.entity.*;
//import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraft.world.entity.ai.control.FlyingMoveControl;
//import net.minecraft.world.entity.ai.goal.Goal;
//import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
//import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
//import net.minecraft.world.entity.ai.navigation.PathNavigation;
//import net.minecraft.world.entity.ai.util.RandomPos;
//import net.minecraft.world.entity.animal.FlyingAnimal;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.entity.projectile.Arrow;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.ServerLevelAccessor;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.material.Fluid;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.fmllegacy.common.registry.IEntityAdditionalSpawnData;
//import net.minecraftforge.fmllegacy.network.NetworkHooks;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import javax.annotation.Nullable;
//import java.util.EnumSet;
//import java.util.Random;
//
//public class FireflyEntity extends PathfinderMob implements FlyingAnimal, IEntityAdditionalSpawnData {
//    public float alpha = 0f;
//    public float prevAlpha = 0.01f;
//    private int flickerPeriod;//+ new Random().nextInt(10) ; //40
//    private int offset;//new Random().nextInt(Math.abs(this.flickerPeriod));
//
//    public FireflyEntity(EntityType<? extends PathfinderMob> type, Level world) {
//        super(type, world);
//        xpReward = 1;
//        setNoAi(false);
//        this.moveControl = new FlyingMoveControl(this, 10, true);
//        //this.navigator = new FlyingPathNavigator(this, this.world);
//
//    }
//
//    @Nullable
//    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData data, @Nullable CompoundTag compound) {
//        data = super.finalizeSpawn(world, difficulty, spawnReason, data, compound);
//
//        //this.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(Items.REDSTONE_TORCH));
//
//        return data;
//    }
//
//
//    @Override
//    protected PathNavigation createNavigation(Level worldIn) {
//        FlyingPathNavigation flyingpathnavigator = new FlyingPathNavigation(this, worldIn) {
//            public boolean isStableDestination(BlockPos pos) {
//                return !this.level.getBlockState(pos.below()).isAir();
//            }
//        };
//        flyingpathnavigator.setCanOpenDoors(false);
//        flyingpathnavigator.setCanFloat(false);
//        flyingpathnavigator.setCanPassDoors(true);
//        return flyingpathnavigator;
//    }
//
//
//    public static boolean canSpawnOn(EntityType<? extends Mob> firefly, LevelAccessor worldIn, MobSpawnType reason, BlockPos pos, Random random) {
//        BlockState blockstate = worldIn.getBlockState(pos.below());
//        if (pos.getY() <= worldIn.getSeaLevel()) {
//            return false;
//        }
//        return (blockstate.is(BlockTags.LEAVES) || blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(BlockTags.LOGS) || blockstate.is(Blocks.AIR)) && worldIn.getRawBrightness(pos, 0) > 8;
//    }
//
//    @Override
//    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType spawnReasonIn) {
//        return !this.level.isDay() && !this.level.isThundering();
//    }
//
//    @Override
//    public boolean shouldRenderAtSqrDistance(double distance) {
//        return Math.abs(distance) < 3500;
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//    }
//
//    @Override
//    public void readSpawnData(FriendlyByteBuf buffer) {
//        this.offset = buffer.readInt();
//        this.flickerPeriod = buffer.readInt();
//    }
//
//    @Override
//    public void writeSpawnData(FriendlyByteBuf buffer) {
//        this.flickerPeriod = ServerConfigs.cached.FIREFLY_PERIOD + this.random.nextInt(10);
//        this.offset = this.random.nextInt(this.flickerPeriod / 2);
//        buffer.writeInt(this.offset);
//        buffer.writeInt(this.flickerPeriod);
//    }
//
//    @Override
//    public boolean isAlive() {
//        return true;
//    }
//
//    @Override
//    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
//        return sizeIn.height / 2.0F;
//    }
//
//    @Override
//    public boolean canBeLeashed(Player player) {
//        return false;
//    }
//
//    @Override
//    public boolean isIgnoringBlockTriggers() {
//        return true;
//    }
//
//    @Override
//    public boolean isPushable() {
//        return false;
//    }
//
//    @Override
//    protected void doPush(Entity entityIn) {
//    }
//
//    @Override
//    protected void pushEntities() {
//    }
//
//    @Override
//    protected boolean isMovementNoisy() {
//        return false;
//    }
//
//    @Override
//    public Packet<?> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }
//
//    @Override
//    protected void registerGoals() {
//        super.registerGoals();
//        this.goalSelector.addGoal(0, new RandomLookAroundGoal(this));
//        // this.goalSelector.addGoal(1, new SwimGoal(this));
//        this.goalSelector.addGoal(1, new FireflyEntity.WanderGoal());
//    }
//
//    protected void customServerAiStep() {
//        super.customServerAiStep();
//    }
//
//    @Override
//    public MobType getMobType() {
//        return MobType.UNDEFINED;
//    }
//
//    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
//        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
//    }
//
//    @Override
//    public net.minecraft.sounds.SoundEvent getHurtSound(DamageSource ds) {
//        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.hurt"));
//    }
//
//    @Override
//    public net.minecraft.sounds.SoundEvent getDeathSound() {
//        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.death"));
//    }
//
//    //TODO: test this
//    protected void jumpInLiquid(Tag<Fluid> fluidTag) {
//        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
//    }
//
//    @Override
//    public boolean causeFallDamage(float l, float d, DamageSource source) {
//        return false;
//    }
//
//    @Override
//    public boolean hurt(DamageSource source, float amount) {
//        if (source.getDirectEntity() instanceof Arrow)
//            return false;
//        if (source == DamageSource.FALL)
//            return false;
//        if (source == DamageSource.CACTUS)
//            return false;
//        return super.hurt(source, amount);
//    }
//
//    public static AttributeSupplier.Builder setCustomAttributes() {
//        return Mob.createMobAttributes()
//                .add(Attributes.FOLLOW_RANGE, 48.0D)
//                .add(Attributes.MOVEMENT_SPEED, ServerConfigs.entity.FIREFLY_SPEED.get())
//                .add(Attributes.MAX_HEALTH, 1)
//                .add(Attributes.ARMOR, 0)
//                .add(Attributes.ATTACK_DAMAGE, 0D)
//                .add(Attributes.FLYING_SPEED, ServerConfigs.entity.FIREFLY_SPEED.get());
//    }
//
//    @Override
//    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
//    }
//
//    @Override
//    public void setNoGravity(boolean ignored) {
//        super.setNoGravity(true);
//    }
//
//    public void aiStep() {
//        super.aiStep();
//        //this.particleCooldown--;
//
//        //despawn when entity is not lit
//        if (this.alpha == 0f && !this.level.isClientSide) {
//
//            if (this.level.isRaining() && this.random.nextFloat() < 0.1) {
//                this.remove(RemovalReason.KILLED);
//            }
//            if (ServerConfigs.cached.FIREFLY_DESPAWN) {
//                long dayTime = this.level.getDayTime() % 24000;
//                if (dayTime > 23500 || dayTime < 12500 && this.random.nextFloat() < 0.1)
//                    this.remove(RemovalReason.KILLED);
//            }
//
//        }
//
//        //this.flickerCounter++;
//
//
//        this.prevAlpha = this.alpha;
//        float a = (float) ClientConfigs.cached.FIREFLY_INTENSITY; //0.3
//        float p = (float) ClientConfigs.cached.FIREFLY_EXPONENT;
//        float time = this.tickCount + this.offset;
//        boolean w = this.level.isClientSide;
//
//        this.alpha = Math.max(((1 - a) * Mth.sin(time * ((float) Math.PI * 2 / this.flickerPeriod)) + a), 0);
//        if (this.alpha != 0) this.alpha = (float) Math.pow(this.alpha, p);
//        //this.alpha =  Math.max( ( (1-p)*MathHelper.sin(this.ticksExisted * ((float) Math.PI / this.flickerPeriod))+p), 0);
//
//        if (this.level.isClientSide) {
//            if (prevAlpha == 0 && this.alpha != 0) this.switchLight(true);
//            else if (prevAlpha != 0 && this.alpha == 0) this.switchLight(false);
//        }
//
//        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.65D, 1.0D));
//        this.setDeltaMovement(this.getDeltaMovement().add(0.02 * (this.random.nextDouble() - 0.5), 0.03 * (this.random.nextDouble() - 0.5),
//                0.02 * (this.random.nextDouble() - 0.5)));
//    }
//
//    private void switchLight(boolean on) {
//        if (on) this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.MAGMA_BLOCK));
//        else this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
//    }
//
//
//    //bee code
//    class WanderGoal extends Goal {
//        WanderGoal() {
//            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
//        }
//
//
//        //Returns whether execution should begin. You can also read and cache any state
//        //necessary for execution in this method as well.
//
//        public boolean canUse() {
//            return FireflyEntity.this.navigation.isDone() && FireflyEntity.this.random.nextInt(50) == 0;
//        }
//
//
//        //Returns whether an in-progress EntityAIBase should continue executing
//
//        public boolean canContinueToUse() {
//            return FireflyEntity.this.navigation.isInProgress();
//        }
//
//
//        //Execute a one shot task or start executing a continuous task
//
//        //TODO: seems to lag servers->getPathToPos
//        public void start() {
//            Vec3 vec3d = this.getRandomLocation();
//            if (vec3d != null) {
//                FireflyEntity.this.navigation.moveTo(FireflyEntity.this.navigation.createPath(new BlockPos(vec3d), 1), 1.0D);
//            }
//        }
//
//        @Nullable
//        private Vec3 getRandomLocation() {
//            Vec3 vec3d;
//            vec3d = FireflyEntity.this.getViewVector(0.0F);
//            int i = 8;
//            Vec3 vec3d2 = RandomPos.getAboveLandPos(FireflyEntity.this, 8, 7, vec3d, ((float) Math.PI / 2F), 2, 1);
//            return vec3d2 != null
//                    ? vec3d2
//                    : RandomPos.getAirPos(FireflyEntity.this, 8, 4, -2, vec3d, (float) Math.PI / 2F);
//        }
//    }
//}