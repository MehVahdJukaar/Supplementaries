package net.mehvahdjukaar.supplementaries.entities.goals;

import net.mehvahdjukaar.supplementaries.block.blocks.FodderBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Random;

public class EatFodderGoal extends MoveToBlockGoal {

    private final Animal animal;
    private final int blockBreakingTime;
    private int ticksSinceReachedGoal;
    protected int lastBreakProgress = -1;

    public EatFodderGoal(Animal entity, double speedModifier, int searchRange, int verticalSearchRange, int breakTime) {
        super(entity, speedModifier, searchRange, verticalSearchRange);
        this.animal = entity;
        this.blockBreakingTime = breakTime;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.animal.canFallInLove() || this.animal.getAge() > 0) return false;
        if (!net.minecraftforge.common.ForgeHooks.canEntityDestroy(this.animal.level, this.blockPos, this.animal)) {
            return false;
        } else if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else if (this.tryFindBlock()) {
            //cooldown between attempts
            this.nextStartTick = 600;
            return true;
        } else {
            //no blocks around. cooldown
            this.nextStartTick = nextStartTick(this.mob);
            return false;
        }
    }

    //TODO: finish this. still buggy
    @Override
    public boolean canContinueToUse() {
        //try is low so they dont get stuck
        return this.tryTicks >= -100 && this.tryTicks <= 200 && this.isValidTarget(this.mob.level, this.blockPos);
    }

    private boolean tryFindBlock() {
        return this.blockPos != null && this.isValidTarget(this.mob.level, this.blockPos) || this.findNearestBlock();
    }


    @Override
    protected int nextStartTick(PathfinderMob p_203109_1_) {
        return 800 + p_203109_1_.getRandom().nextInt(400);
    }

    @Override
    public void stop() {
        super.stop();
        this.animal.fallDistance = 1.0F;
    }

    @Override
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
    }

    @Override
    public double acceptedDistance() {
        return 1.4;
    }

    private static final BlockState FODDER_STATE = ModRegistry.FODDER.get().defaultBlockState();

    @Override
    public void tick() {
        super.tick();
        Level world = this.animal.level;
        //BlockPos blockpos = this.removerMob.blockPosition();
        //BlockPos blockpos1 = this.getPosWithBlock(blockpos, world);

        Random random = this.animal.getRandom();
        if (this.isReachedTarget()) {

            //prevents stopping while eating
            this.tryTicks--;

            BlockPos targetPos = this.getMoveToTarget().below();
            Vec3 vector3d = Vec3.atBottomCenterOf(targetPos);
            this.mob.getLookControl().setLookAt(vector3d.x(), vector3d.y(), vector3d.z());
            if (this.ticksSinceReachedGoal > 0) {

                if (!world.isClientSide && ticksSinceReachedGoal % 2 == 0) {

                    ((ServerLevel) world).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FODDER_STATE),
                            (double) targetPos.getX() + 0.5D, (double) targetPos.getY() + 0.7D, (double) targetPos.getZ() + 0.5D, 3,
                            ((double) random.nextFloat() - 0.5D) * 0.08D, ((double) random.nextFloat() - 0.5D) * 0.08D, ((double) random.nextFloat() - 0.5D) * 0.08D, 0.15F);
                }
            }
            if (this.ticksSinceReachedGoal == 1 && this.animal instanceof Sheep) {
                world.broadcastEntityEvent(this.mob, (byte) 10);
            }

            //breaking animation
            int k = (int) ((float) this.ticksSinceReachedGoal / (float) this.blockBreakingTime * 10.0F);
            if (k != this.lastBreakProgress) {
                this.mob.level.destroyBlockProgress(this.mob.getId(), this.blockPos, k);
                this.lastBreakProgress = k;
            }

            //break block
            if (this.ticksSinceReachedGoal > this.blockBreakingTime) {
                int layers = world.getBlockState(targetPos).getValue(FodderBlock.LAYERS);
                if (layers > 1) {
                    world.levelEvent(2001, targetPos, Block.getId(FODDER_STATE));
                    world.setBlock(targetPos, FODDER_STATE.setValue(FodderBlock.LAYERS, layers - 1), 2);
                } else {
                    world.destroyBlock(targetPos, false);
                }
                if (!world.isClientSide) {
                    ((ServerLevel) world).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.animal.getX(), this.animal.getY(), this.animal.getZ(), 5,
                            this.animal.getBbWidth() / 2f, this.animal.getBbHeight() / 2f, this.animal.getBbWidth() / 2f, 0);
                }
                //so it stops
                this.nextStartTick = this.nextStartTick(this.mob);
                this.tryTicks = 800;
                if (!this.animal.isBaby()) this.animal.setInLove(null);
                this.animal.ate();
            }

            ++this.ticksSinceReachedGoal;
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        ChunkAccess chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return false;
        } else {
            return chunk.getBlockState(pos).is(ModRegistry.FODDER.get()) && chunk.getBlockState(pos.above()).isAir();
        }
    }
}
