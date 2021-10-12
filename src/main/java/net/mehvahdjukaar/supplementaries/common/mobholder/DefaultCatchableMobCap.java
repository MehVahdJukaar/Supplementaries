package net.mehvahdjukaar.supplementaries.common.mobholder;

import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.items.AbstractMobContainerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;

public class DefaultCatchableMobCap<T extends Entity> extends BaseCatchableMobCap<T> {

    //calculated every time is loaded
    private final CapturedMobsHelper.CapturedMobConfigProperties properties;

    //client only
    protected float jumpY = 0;
    protected float prevJumpY = 0;
    protected float yVel = 0;

    public DefaultCatchableMobCap(T entity) {
        super(entity);
        this.properties = CapturedMobsHelper.getType(entity);
    }

    @Override
    public float getHitBoxWidthIncrement() {
        return this.properties.getWidth();
    }

    @Override
    public float getHitBoxHeightIncrement() {
        return this.properties.getHeight();
    }

    @Override
    public int getLightLevel() {
        return this.properties.getLightLevel();
    }

    //3
    @Override
    public boolean canBeCaughtWithItem(Item item) {
        //only allows small slimes
        if (mob instanceof SlimeEntity && ((SlimeEntity) mob).getSize() > 1) {
            return false;
        }
        //hard coding bees to work with resourceful bees
        if (mob instanceof BeeEntity) {
            return true;
        }
        if (item instanceof AbstractMobContainerItem) {
            //we went full circle lol
            return ((AbstractMobContainerItem) item).canItemCatch(mob);
        }
        return false;
    }

    @Override
    public boolean canBeCaughtWithJar() {
        return false;
    }

    @Override
    public boolean canBeCaughtWithTintedJar() {
        return false;
    }

    @Override
    public boolean canBeCaughtWithCage() {
        return false;
    }

    @Override
    public void onContainerWaterlogged(boolean waterlogged) {
        if (this.mob instanceof WaterMobEntity && this.mob.isInWater() != waterlogged) {
            this.mob.wasTouchingWater = waterlogged;
            Pair<Float, Float> dim = MobContainer.calculateMobDimensionsForContainer(this, this.containerWidth, this.containerHeight, waterlogged);
            double py = dim.getRight() + 0.0001;
            mob.setPos(this.mob.getX(), py, this.mob.getZ());
            mob.yOld = py;
        }
    }

    @Override
    public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
        if(world.isClientSide) {
            if (this.properties.isFloating()) {
                this.jumpY = 0.04f * MathHelper.sin(mob.tickCount / 10f) - 0.03f;
            }
            mob.yOld = mob.getY();
            float dy = jumpY - prevJumpY;
            if (dy != 0) {
                mob.setPos(mob.getX(), mob.getY() + dy, mob.getZ());
            }
            this.prevJumpY = this.jumpY;
        }
    }

    //cat.setInSittingPose(true);
    //TODO: finish this so they get lowered when not in water
    @Override
    public boolean isFlyingMob(boolean waterlogged) {
        CapturedMobsHelper.AnimationCategory cat = this.getAnimationCategory();
        return !cat.isLand() && (cat.isFlying() || mob.isNoGravity() || mob instanceof IFlyingAnimal ||
                mob.isIgnoringBlockTriggers() || (mob instanceof WaterMobEntity && waterlogged));
    }

    public CapturedMobsHelper.AnimationCategory getAnimationCategory() {
        return properties.getCategory();
    }

    public static<E extends Entity> ICatchableMob getDefaultCap(E e) {
        if (e.level.isClientSide && e instanceof LivingEntity && ClientConfigs.block.TICKLE_MOBS.get()
                .contains(e.getType().getRegistryName().toString())) return new ClientTickableAnim((LivingEntity)e);
        else if (e instanceof SquidEntity) return new DoubleSideTickableAnim((LivingEntity) e);
        //else if (e instanceof WaterMobEntity) return WATER_MOB;
        else if (e instanceof SlimeEntity) return new SlimeAnim((SlimeEntity) e);
        else if (e instanceof ParrotEntity) return new ParrotAnim((ParrotEntity) e);
        //else if (e instanceof CatEntity) return CAT;
        else if (e instanceof RabbitEntity) return new RabbitAnim((RabbitEntity) e);
        else if (e instanceof ChickenEntity) return new ChickenAnim((ChickenEntity) e);
        else if (e instanceof EndermiteEntity) return new EndermiteAnim((EndermiteEntity) e);
        return new DefaultCatchableMobCap<>(e);
    }

    public static class SlimeAnim extends DefaultCatchableMobCap<SlimeEntity> {

        public SlimeAnim(SlimeEntity entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
            if (world.isClientSide) {

                mob.squish += (mob.targetSquish - mob.squish) * 0.5F;
                mob.oSquish = mob.squish;
                //move
                if (this.yVel != 0)
                    this.jumpY = Math.max(0, this.jumpY + this.yVel);
                if (jumpY != 0) {
                    //decelerate
                    this.yVel = this.yVel - 0.010f;
                }
                //on ground
                else {
                    if (this.yVel != 0) {
                        //land
                        this.yVel = 0;
                        mob.targetSquish = -0.5f;
                    }
                    if (world.getRandom().nextFloat() > 0.985) {
                        //jump
                        this.yVel = 0.08f;
                        mob.targetSquish = 1.0F;
                    }
                }
                mob.targetSquish *= 0.6F;

                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }


    }

    public static class ChickenAnim extends DefaultCatchableMobCap<ChickenEntity> {

        public ChickenAnim(ChickenEntity entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
            Random rand = world.getRandom();
            if (!world.isClientSide) {
                if (--mob.eggTime <= 0) {
                    mob.spawnAtLocation(Items.EGG);
                    mob.eggTime = rand.nextInt(6000) + 6000;
                }
            } else {
                mob.aiStep();
                if (world.random.nextFloat() > (mob.isOnGround() ? 0.99 : 0.88)) {
                    mob.setOnGround(!mob.isOnGround());
                }
                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }

    public static class RabbitAnim extends DefaultCatchableMobCap<RabbitEntity> {

        public RabbitAnim(RabbitEntity entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
            if (world.isClientSide) {

                //move
                if (this.yVel != 0)
                    this.jumpY = Math.max(0, this.jumpY + this.yVel);
                if (jumpY != 0) {
                    //decelerate
                    this.yVel = this.yVel - 0.017f;
                }
                //on ground
                else {
                    if (this.yVel != 0) {
                        //land
                        this.yVel = 0;
                    }
                    if (world.random.nextFloat() > 0.985) {
                        //jump
                        this.yVel = 0.093f;
                        mob.startJumping();
                    }
                }
                //handles actual animation without using reflections
                mob.aiStep();
                //TODO: living tick causes collisions to happen
                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }

    public static class ParrotAnim extends DefaultCatchableMobCap<ParrotEntity> {

        public ParrotAnim(ParrotEntity entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
            if (world.isClientSide) {

                mob.aiStep();
                boolean p = mob.isPartyParrot();
                mob.setOnGround(p);
                this.jumpY = p ? 0 : 0.0625f;

                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }

    public static class EndermiteAnim extends DefaultCatchableMobCap<EndermiteEntity> {

        public EndermiteAnim(EndermiteEntity entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
            if (world.isClientSide) {

                if (world.random.nextFloat() > 0.7f) {
                    world.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5f, pos.getY() + 0.2f,
                            pos.getZ() + 0.5f, (world.random.nextDouble() - 0.5D) * 2.0D,
                            -world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D);
                }

                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }

    public static class DoubleSideTickableAnim extends DefaultCatchableMobCap<LivingEntity> {

        public DoubleSideTickableAnim(LivingEntity entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
            mob.aiStep();
            if (world.isClientSide) {
                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }

    public static class ClientTickableAnim extends DefaultCatchableMobCap<LivingEntity> {

        public ClientTickableAnim(LivingEntity entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(World world, BlockPos pos, float mobScale, CompoundNBT tag) {
            if (world.isClientSide) {
                mob.aiStep();
                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }
}
