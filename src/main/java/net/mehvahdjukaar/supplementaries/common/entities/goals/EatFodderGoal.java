package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.block.blocks.FodderBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraftforge.common.ForgeHooks;

import java.util.EnumSet;

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
        if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else if (this.blockPos != BlockPos.ZERO && !ForgeHooks.canEntityDestroy(this.animal.level, this.blockPos, this.animal)) {
            return false;
        }
        else if (this.tryFindBlock()) {
            //cooldown between attempts if blocks are around
            this.nextStartTick = 600;
            return true;
        } else {
            //no blocks around. cooldown
            this.nextStartTick = nextStartTick(this.mob);
            return false;
        }
    }

    //TODO: finish this. still buggy. improve
    @Override
    public boolean canContinueToUse() {
        //try is low so they dont get stuck
        return this.tryTicks >= -100 && this.tryTicks <= 200 && this.isValidTarget(this.mob.level, this.blockPos);
    }

    private boolean tryFindBlock() {
        return this.blockPos != BlockPos.ZERO && this.isValidTarget(this.mob.level, this.blockPos) || this.findNearestBlock();
    }


    @Override
    protected int nextStartTick(PathfinderMob pCreature) {
        return 800 + pCreature.getRandom().nextInt(400);
    }

    @Override
    public void stop() {
        super.stop();
        this.animal.fallDistance = 1.0F;
        this.blockPos = BlockPos.ZERO;
    }

    @Override
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
    }

    @Override
    public double acceptedDistance() {
        return 1.5;
    }

    private static final BlockState FODDER_STATE = ModRegistry.FODDER.get().defaultBlockState();

    @Override
    public void tick() {
        super.tick();
        Level world = this.animal.level;
        //BlockPos blockpos = this.removerMob.blockPosition();
        //BlockPos blockpos1 = this.getPosWithBlock(blockpos, world);

        RandomSource random = this.animal.getRandom();
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
                BlockState state = world.getBlockState(targetPos);
                if(state.is(ModRegistry.FODDER.get())) {
                    int layers = state.getValue(FodderBlock.LAYERS);
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
                    if (!this.animal.isBaby()) this.animal.setInLove(null);
                    this.animal.ate();
                }
                //so it stops
                this.nextStartTick = this.nextStartTick(this.mob);
                this.tryTicks = 800;

            }

            ++this.ticksSinceReachedGoal;
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        ChunkAccess chunk = world.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk == null) {
            return false;
        } else {
            return chunk.getBlockState(pos).is(ModRegistry.FODDER.get()) && chunk.getBlockState(pos.above()).isAir();
        }
    }

}
