package net.mehvahdjukaar.supplementaries.common.capabilities.mobholder;

import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
        if (mob instanceof Slime slime && slime.getSize() > 1) {
            return false;
        }
        //hard coding bees to work with resourceful bees
        if (mob instanceof Bee) {
            return true;
        }
        if (item instanceof AbstractMobContainerItem containerItem) {
            //we went full circle lol
            return containerItem.canItemCatch(mob);
        }
        return false;
    }

    @Override
    public void onContainerWaterlogged(boolean waterlogged) {
        if (this.mob instanceof WaterAnimal && this.mob.isInWater() != waterlogged) {
            this.mob.wasTouchingWater = waterlogged;
            Pair<Float, Float> dim = MobContainer.calculateMobDimensionsForContainer(this, this.containerWidth, this.containerHeight, waterlogged);
            double py = dim.getRight() + 0.0001;
            mob.setPos(this.mob.getX(), py, this.mob.getZ());
            mob.yOld = py;
        }
    }

    @Override
    public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
        if (world.isClientSide) {
            if (this.properties.isFloating()) {
                this.jumpY = 0.04f * Mth.sin(mob.tickCount / 10f) - 0.03f;
            }
            mob.yOld = mob.getY();
            float dy = jumpY - prevJumpY;
            if (dy != 0) {
                mob.setPos(mob.getX(), mob.getY() + dy, mob.getZ());
            }
            this.prevJumpY = this.jumpY;
        }
    }

    @Override
    public boolean isFlyingMob(boolean waterlogged) {
        CapturedMobsHelper.AnimationCategory cat = this.getAnimationCategory();
        return !cat.isLand() && (cat.isFlying() || mob.isNoGravity() || mob instanceof FlyingAnimal ||
                mob.isIgnoringBlockTriggers() || (mob instanceof WaterAnimal && waterlogged));
    }

    public CapturedMobsHelper.AnimationCategory getAnimationCategory() {
        return properties.getCategory();
    }

    public static <E extends Entity> ICatchableMob getDefaultCap(E e) {
        if (e.level.isClientSide && e instanceof LivingEntity && ClientConfigs.block.TICKLE_MOBS.get()
                .contains(e.getType().getRegistryName().toString())) return new ClientTickableAnim((LivingEntity) e);
        else if (e instanceof Squid) return new DoubleSideTickableAnim((LivingEntity) e);
            //else if (e instanceof WaterMobEntity) return WATER_MOB;
        else if (e instanceof Slime) return new SlimeAnim((Slime) e);
        else if (e instanceof Parrot) return new ParrotAnim((Parrot) e);
            //else if (e instanceof CatEntity) return CAT;
        else if (e instanceof Rabbit) return new RabbitAnim((Rabbit) e);
        else if (e instanceof Chicken) return new ChickenAnim((Chicken) e);
        else if (e instanceof Endermite) return new EndermiteAnim((Endermite) e);
        return new DefaultCatchableMobCap<>(e);
    }

    public static class SlimeAnim extends DefaultCatchableMobCap<Slime> {

        public SlimeAnim(Slime entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
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

    public static class ChickenAnim extends DefaultCatchableMobCap<Chicken> {

        public ChickenAnim(Chicken entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
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

    public static class RabbitAnim extends DefaultCatchableMobCap<Rabbit> {

        public RabbitAnim(Rabbit entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
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

    public static class ParrotAnim extends DefaultCatchableMobCap<Parrot> {

        public ParrotAnim(Parrot entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
            if (world.isClientSide) {

                mob.aiStep();
                boolean p = mob.isPartyParrot();
                mob.setOnGround(p);
                this.jumpY = p ? 0 : 0.0625f;

                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }

    public static class EndermiteAnim extends DefaultCatchableMobCap<Endermite> {

        public EndermiteAnim(Endermite entity) {
            super(entity);
        }

        @Override
        public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
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
        public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
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
        public void tickInsideContainer(Level world, BlockPos pos, float mobScale, CompoundTag tag) {
            if (world.isClientSide) {
                mob.aiStep();
                super.tickInsideContainer(world, pos, mobScale, tag);
            }
        }
    }
}
