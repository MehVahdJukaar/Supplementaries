package net.mehvahdjukaar.supplementaries.common.misc.mob_container;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class BuiltinAnimation<T extends Entity> {

    protected float jumpY = 0;
    protected float prevJumpY = 0;
    protected float yVel = 0;

    void tick(T mob, Level world, BlockPos pos){
        if (world.isClientSide) {
            mob.yOld = mob.getY();
            float dy = jumpY - prevJumpY;
            if (dy != 0) {
                mob.setPos(mob.getX(), mob.getY() + dy, mob.getZ());
            }
            this.prevJumpY = this.jumpY;
        }
    }

    @Nullable
    public static <E extends Entity> BuiltinAnimation<E> get(E entity, Type type){
        if(type == Type.BUILTIN){
            if(entity instanceof Slime slime)return new SlimeAnim(slime);
            else if(entity instanceof Chicken chicken)return new ChickenAnim(chicken);
            else if(entity instanceof Rabbit rabbit)return new RabbitAnim(rabbit);
            else if(entity instanceof Parrot parrot)return new ParrotAnim(parrot);
            else if(entity instanceof Endermite endermite)return new EndermiteAnim(endermite);
        }else if(type == Type.FLOATING){
           return new FloatingAnim<>(entity);
        }
        return null;
    }

    private static class FloatingAnim<M extends Entity> extends BuiltinAnimation<M> {

        FloatingAnim(M entity){};

        @Override
        public void tick(M mob, Level world, BlockPos pos) {
            if (world.isClientSide) {
                this.jumpY = 0.04f * Mth.sin(mob.tickCount / 10f) - 0.03f;
            }
        }
    }

    private static class SlimeAnim<M extends Slime> extends BuiltinAnimation<M> {

        SlimeAnim(M slime){};

        @Override
        public void tick(M mob, Level world, BlockPos pos) {
            if (world.isClientSide) {

                mob.squish += (mob.targetSquish - mob.squish) * 0.5F;
                mob.oSquish = mob.squish;
                //move
                if (this.yVel != 0)
                    this.jumpY = Math.max(0, this.jumpY + this.yVel);
                if (jumpY != 0) {
                    //decelerate
                    this.yVel = this.yVel - 0.04f;
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
                        this.yVel = 0.153f;
                        mob.targetSquish = 1.0F;
                    }
                }
                mob.targetSquish *= 0.6F;
                super.tick(mob, world, pos);
            }
        }
    }

    private static class ChickenAnim<M extends Chicken> extends BuiltinAnimation<M> {

        public ChickenAnim(Chicken chicken) {
            super();
        }

        @Override
        public void tick(M mob, Level world, BlockPos pos) {
            RandomSource rand = world.getRandom();
            if (!world.isClientSide) {
                if (--mob.eggTime <= 0) {
                    mob.spawnAtLocation(Items.EGG);
                    mob.eggTime = rand.nextInt(6000) + 6000;
                }
            } else {
                mob.aiStep();
                if (world.random.nextFloat() > (mob.onGround() ? 0.99 : 0.88)) {
                    mob.setOnGround(!mob.onGround());
                }
            }
        }
    }

    private static class RabbitAnim<M extends Rabbit> extends BuiltinAnimation<M> {

        public RabbitAnim(Rabbit rabbit) {
            super();
        }

        @Override
        public void tick(M mob, Level world, BlockPos pos) {
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
                super.tick(mob, world, pos);
                //TODO: living tick causes collisions to happen
            }
        }
    }

    private static class ParrotAnim<M extends Parrot> extends BuiltinAnimation<M> {

        public ParrotAnim(Parrot parrot) {
            super();
        }

        @Override
        public void tick(M mob, Level world, BlockPos pos){
            if (world.isClientSide) {

                mob.aiStep();
                boolean p = mob.isPartyParrot();
                mob.setOnGround(p);
                this.jumpY = p ? 0 : 0.0625f;
                super.tick(mob, world, pos);
            }
        }
    }

    private static class EndermiteAnim<M extends Endermite> extends BuiltinAnimation<M> {

        public EndermiteAnim(Endermite endermite) {
            super();
        }

        @Override
        public void tick(M mob, Level world, BlockPos pos) {
            if (world.isClientSide) {

                if (world.random.nextFloat() > 0.7f) {
                    world.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5f, pos.getY() + 0.2f,
                            pos.getZ() + 0.5f, (world.random.nextDouble() - 0.5D) * 2.0D,
                            -world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D);
                }
            }
        }
    }

    public enum Type implements StringRepresentable {
        NONE,
        LAND,
        AIR,
        FLOATING,
        BUILTIN; //hardcoded ones

        public boolean isFlying() {
            return this == AIR || this == FLOATING;
        }

        public boolean isLand() {
            return this == LAND;
        }

        public boolean isFloating() {
            return this == FLOATING;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
    }
}
