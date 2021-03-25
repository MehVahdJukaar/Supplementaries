package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class FireflyEntity extends CreatureEntity implements IFlyingAnimal, IEntityAdditionalSpawnData {
    public float alpha = 0f;
    public float prevAlpha = 0.01f;
    private int flickerPeriod;//+ new Random().nextInt(10) ; //40
    private int offset;//new Random().nextInt(Math.abs(this.flickerPeriod));

    public FireflyEntity(EntityType<? extends CreatureEntity> type, World world) {
        super(type, world);
        xpReward = 1;
        setNoAi(false);
        this.moveControl = new FlyingMovementController(this, 10, true);
        //this.navigator = new FlyingPathNavigator(this, this.world);

    }

    @Override
    protected PathNavigator createNavigation(World worldIn) {
        FlyingPathNavigator flyingpathnavigator = new FlyingPathNavigator(this, worldIn) {
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(false);
        flyingpathnavigator.setCanPassDoors(true);
        return flyingpathnavigator;
    }


    public static boolean canSpawnOn(EntityType<? extends MobEntity> firefly, IWorld worldIn, SpawnReason reason, BlockPos pos, Random random) {
        BlockState blockstate = worldIn.getBlockState(pos.below());
        if (pos.getY() <= worldIn.getSeaLevel()) {return false;}
        return (blockstate.is(BlockTags.LEAVES) || blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(BlockTags.LOGS) || blockstate.is(Blocks.AIR)) && worldIn.getRawBrightness(pos, 0) > 8;
    }

    @Override
    public boolean checkSpawnRules(IWorld world, SpawnReason spawnReasonIn)
    {
        return !this.level.isDay() && !this.level.isThundering();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return Math.abs(distance) < 3500;
    }

    @Override
    public void tick() {
        super.tick();


        //despawn when entity is not lit
        if (this.alpha == 0f && !this.level.isClientSide){

            if(this.level.isRaining() && this.random.nextFloat()<0.1) {
                this.remove();
            }
            if(ServerConfigs.cached.FIREFLY_DESPAWN) {
                long dayTime = this.level.getDayTime() % 24000;
                if (dayTime > 23500 || dayTime < 12500 && this.random.nextFloat() < 0.1)
                    this.remove();
            }

        }

        //this.flickerCounter++;
        this.prevAlpha = this.alpha;
        float a = (float) ClientConfigs.cached.FIREFLY_INTENSITY; //0.3
        float p = (float) ClientConfigs.cached.FIREFLY_EXPONENT;
        float time = this.tickCount+this.offset;
        boolean w = this.level.isClientSide;

        this.alpha = Math.max(((1-a)*MathHelper.sin(time * ((float)Math.PI*2 / this.flickerPeriod))+a),0);
        if (this.alpha!=0)this.alpha= (float) Math.pow(this.alpha,p);
        //this.alpha =  Math.max( ( (1-p)*MathHelper.sin(this.ticksExisted * ((float) Math.PI / this.flickerPeriod))+p), 0);


        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.65D, 1.0D));
        this.setDeltaMovement(this.getDeltaMovement().add(0.02 * (this.random.nextDouble() - 0.5), 0.03 * (this.random.nextDouble() - 0.5),
                0.02 * (this.random.nextDouble() - 0.5)));


    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.offset = buffer.readInt();
        this.flickerPeriod = buffer.readInt();
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        this.flickerPeriod = ServerConfigs.cached.FIREFLY_PERIOD + this.random.nextInt(10);
        this.offset = this.random.nextInt(this.flickerPeriod/2);
        buffer.writeInt(this.offset);
        buffer.writeInt(this.flickerPeriod);
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return sizeIn.height / 2.0F;
    }

    @Override
    public boolean canBeLeashed(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entityIn) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new LookRandomlyGoal(this));
        // this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(1, new FireflyEntity.WanderGoal());
    }

    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    @Override
    public CreatureAttribute getMobType() {
        return CreatureAttribute.UNDEFINED;
    }

    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
    }

    @Override
    public net.minecraft.util.SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.hurt"));
    }

    @Override
    public net.minecraft.util.SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.death"));
    }

    //TODO: test this
    protected void jumpInLiquid(ITag<Fluid> fluidTag) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
    }

    @Override
    public boolean causeFallDamage(float l, float d) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ArrowEntity)
            return false;
        if (source == DamageSource.FALL)
            return false;
        if (source == DamageSource.CACTUS)
            return false;
        return super.hurt(source, amount);
    }

    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, ServerConfigs.entity.FIREFLY_SPEED.get())
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 0D)
                .add(Attributes.FLYING_SPEED, ServerConfigs.entity.FIREFLY_SPEED.get());
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    public void aiStep() {
        super.aiStep();
        //this.particleCooldown--;

    }



    //bee code
    class WanderGoal extends Goal {
        WanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }


        //Returns whether execution should begin. You can also read and cache any state
        //necessary for execution in this method as well.

        public boolean canUse() {
            return FireflyEntity.this.navigation.isDone() && FireflyEntity.this.random.nextInt(50) == 0;
        }


        //Returns whether an in-progress EntityAIBase should continue executing

        public boolean canContinueToUse() {
            return FireflyEntity.this.navigation.isInProgress();
        }


        //Execute a one shot task or start executing a continuous task

        //TODO: seems to lag servers->getPathToPos
        public void start() {
            Vector3d vec3d = this.getRandomLocation();
            if (vec3d != null) {
                FireflyEntity.this.navigation.moveTo(FireflyEntity.this.navigation.createPath(new BlockPos(vec3d), 1), 1.0D);
            }
        }

        @Nullable
        private Vector3d getRandomLocation() {
            Vector3d vec3d;
            vec3d = FireflyEntity.this.getViewVector(0.0F);
            int i = 8;
            Vector3d vec3d2 = RandomPositionGenerator.getAboveLandPos(FireflyEntity.this, 8, 7, vec3d, ((float) Math.PI / 2F), 2, 1);
            return vec3d2 != null
                    ? vec3d2
                    : RandomPositionGenerator.getAirPos(FireflyEntity.this, 8, 4, -2, vec3d, (float) Math.PI / 2F);
        }
    }
}