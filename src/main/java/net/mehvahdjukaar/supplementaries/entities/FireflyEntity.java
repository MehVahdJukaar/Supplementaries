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
        experienceValue = 1;
        setNoAI(false);
        this.moveController = new FlyingMovementController(this, 10, true);
        //this.navigator = new FlyingPathNavigator(this, this.world);

    }

    @Override
    protected PathNavigator createNavigator(World worldIn) {
        FlyingPathNavigator flyingpathnavigator = new FlyingPathNavigator(this, worldIn) {
            public boolean canEntityStandOnPos(BlockPos pos) {
                return !this.world.getBlockState(pos.down()).isAir();
            }
        };
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanSwim(false);
        flyingpathnavigator.setCanEnterDoors(true);
        return flyingpathnavigator;
    }


    public static boolean canSpawnOn(EntityType<? extends MobEntity> firefly, IWorld worldIn, SpawnReason reason, BlockPos pos, Random random) {
        BlockState blockstate = worldIn.getBlockState(pos.down());
        if (pos.getY() <= worldIn.getSeaLevel()) {return false;}
        return (blockstate.isIn(BlockTags.LEAVES) || blockstate.isIn(Blocks.GRASS_BLOCK) || blockstate.isIn(BlockTags.LOGS) || blockstate.isIn(Blocks.AIR)) && worldIn.getLightSubtracted(pos, 0) > 8;
    }

    @Override
    public boolean canSpawn(IWorld world, SpawnReason spawnReasonIn)
    {
        return !this.world.isDaytime() && !this.world.isThundering();
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return Math.abs(distance) < 3500;
    }

    @Override
    public void tick() {
        super.tick();


        //despawn when entity is not lit
        if (this.alpha == 0f && !this.world.isRemote){

            if(this.world.isRaining() && this.rand.nextFloat()<0.1) {
                this.remove();
            }
            if(ServerConfigs.cached.FIREFLY_DESPAWN) {
                long dayTime = this.world.getDayTime() % 24000;
                if (dayTime > 23500 || dayTime < 12500 && this.rand.nextFloat() < 0.1)
                    this.remove();
            }

        }

        //this.flickerCounter++;
        this.prevAlpha = this.alpha;
        float a = (float) ClientConfigs.cached.FIREFLY_INTENSITY; //0.3
        float p = (float) ClientConfigs.cached.FIREFLY_EXPONENT;
        float time = this.ticksExisted+this.offset;
        boolean w = this.world.isRemote;

        this.alpha = Math.max(((1-a)*MathHelper.sin(time * ((float)Math.PI*2 / this.flickerPeriod))+a),0);
        if (this.alpha!=0)this.alpha= (float) Math.pow(this.alpha,p);
        //this.alpha =  Math.max( ( (1-p)*MathHelper.sin(this.ticksExisted * ((float) Math.PI / this.flickerPeriod))+p), 0);


        this.setMotion(this.getMotion().mul(1.0D, 0.65D, 1.0D));
        this.setMotion(this.getMotion().add(0.02 * (this.rand.nextDouble() - 0.5), 0.03 * (this.rand.nextDouble() - 0.5),
                0.02 * (this.rand.nextDouble() - 0.5)));


    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.offset = buffer.readInt();
        this.flickerPeriod = buffer.readInt();
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        this.flickerPeriod = ServerConfigs.cached.FIREFLY_PERIOD + this.rand.nextInt(10);
        this.offset = this.rand.nextInt(this.flickerPeriod/2);
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
    public boolean canBeLeashedTo(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
    }

    @Override
    protected void collideWithNearbyEntities() {
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new LookRandomlyGoal(this));
        // this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(1, new FireflyEntity.WanderGoal());
    }

    protected void updateAITasks() {
        super.updateAITasks();
    }

    @Override
    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEFINED;
    }

    protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropSpecialItems(source, looting, recentlyHitIn);
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
    protected void handleFluidJump(ITag<Fluid> fluidTag) {
        this.setMotion(this.getMotion().add(0.0D, 0.01D, 0.0D));
    }

    @Override
    public boolean onLivingFall(float l, float d) {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getImmediateSource() instanceof ArrowEntity)
            return false;
        if (source == DamageSource.FALL)
            return false;
        if (source == DamageSource.CACTUS)
            return false;
        return super.attackEntityFrom(source, amount);
    }

    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 48.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, ServerConfigs.entity.FIREFLY_SPEED.get())
                .createMutableAttribute(Attributes.MAX_HEALTH, 1)
                .createMutableAttribute(Attributes.ARMOR, 0)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 0D)
                .createMutableAttribute(Attributes.FLYING_SPEED, ServerConfigs.entity.FIREFLY_SPEED.get());
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    public void livingTick() {
        super.livingTick();
        this.setNoGravity(true);

        //this.particleCooldown--;

    }



    //bee code
    class WanderGoal extends Goal {
        WanderGoal() {
            this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
        }


        //Returns whether execution should begin. You can also read and cache any state
        //necessary for execution in this method as well.

        public boolean shouldExecute() {
            return FireflyEntity.this.navigator.noPath() && FireflyEntity.this.rand.nextInt(50) == 0;
        }


        //Returns whether an in-progress EntityAIBase should continue executing

        public boolean shouldContinueExecuting() {
            return FireflyEntity.this.navigator.hasPath();
        }


        //Execute a one shot task or start executing a continuous task

        //TODO: seems to lag servers->getPathToPos
        public void startExecuting() {
            Vector3d vec3d = this.getRandomLocation();
            if (vec3d != null) {
                FireflyEntity.this.navigator.setPath(FireflyEntity.this.navigator.getPathToPos(new BlockPos(vec3d), 1), 1.0D);
            }
        }

        @Nullable
        private Vector3d getRandomLocation() {
            Vector3d vec3d;
            vec3d = FireflyEntity.this.getLook(0.0F);
            int i = 8;
            Vector3d vec3d2 = RandomPositionGenerator.findAirTarget(FireflyEntity.this, 8, 7, vec3d, ((float) Math.PI / 2F), 2, 1);
            return vec3d2 != null
                    ? vec3d2
                    : RandomPositionGenerator.findGroundTarget(FireflyEntity.this, 8, 4, -2, vec3d, (float) Math.PI / 2F);
        }
    }
}