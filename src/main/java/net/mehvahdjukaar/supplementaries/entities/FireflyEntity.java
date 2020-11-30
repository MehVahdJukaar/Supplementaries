package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.Block;
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
import net.minecraft.network.IPacket;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class FireflyEntity extends CreatureEntity implements IFlyingAnimal {
    private int particleCooldown = 20;
    public float alpha = 1;
    public float prevAlpha = 1;
    private final int flickerPeriod = ClientConfigs.cached.FIREFLY_PERIOD + new Random().nextInt(10) ; //40


    public FireflyEntity(EntityType<? extends CreatureEntity> type, World world) {
        super(type, world);
        experienceValue = 1;
        setNoAI(false);
        this.moveController = new FlyingMovementController(this, 10, true);
        this.navigator = new FlyingPathNavigator(this, this.world);
        //this.setRenderDistanceWeight(20);
        //this.flickerCounter = (int)(this.rand.nextFloat()*2*this.flickerPeriod);
    }

    public static boolean canSpawnOn(FireflyEntity ce, IWorld worldIn, SpawnReason reason, BlockPos pos, Random random) {
        Block block = worldIn.getBlockState(pos.down()).getBlock();
        return (block.isIn(BlockTags.LEAVES) || block == Blocks.GRASS_BLOCK || block == Blocks.AIR) ;
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
        if (this.alpha == 0){
            long dayTime = this.world.getWorldInfo().getDayTime();
            if (dayTime > 23500 || dayTime < 12500)
                this.remove();
        }
        //this.flickerCounter++;
        this.prevAlpha = this.alpha;
        float a = (float) ClientConfigs.cached.FIREFLY_INTENSITY; //0.3
        float p = (float) ClientConfigs.cached.FIREFLY_EXPONENT;
        this.alpha = (float) Math.pow( Math.max( ( (1-a)*MathHelper.sin(this.ticksExisted * ((float)Math.PI*2 / this.flickerPeriod))+a),0), p);
        //this.alpha =  Math.max( ( (1-p)*MathHelper.sin(this.ticksExisted * ((float) Math.PI / this.flickerPeriod))+p), 0);

        this.setMotion(this.getMotion().mul(1.0D, 0.6D, 1.0D));
        this.setMotion(this.getMotion().add(0.02 * (this.rand.nextDouble() - 0.5), 0.03 * (this.rand.nextDouble() - 0.5),
                0.02 * (this.rand.nextDouble() - 0.5)));
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
        this.goalSelector.addGoal(2, new FireflyEntity.WanderGoal());
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
        return (net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.hurt"));
    }

    @Override
    public net.minecraft.util.SoundEvent getDeathSound() {
        return (net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.death"));
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
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.2)
                .createMutableAttribute(Attributes.MAX_HEALTH, 1)
                .createMutableAttribute(Attributes.ARMOR, 0)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 0D)
                .createMutableAttribute(Attributes.FLYING_SPEED, 0.25);
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

        this.particleCooldown--;

    }
    //bee code
    class WanderGoal extends Goal {
        WanderGoal() {
            this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
        }


        //Returns whether execution should begin. You can also read and cache any state
        //necessary for execution in this method as well.

        public boolean shouldExecute() {
            return FireflyEntity.this.navigator.noPath() && FireflyEntity.this.rand.nextInt(10) == 0;
        }


        //Returns whether an in-progress EntityAIBase should continue executing

        public boolean shouldContinueExecuting() {
            return FireflyEntity.this.navigator.hasPath();
        }


        //Execute a one shot task or start executing a continuous task

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