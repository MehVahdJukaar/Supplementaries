package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.util.*;

// pulley version of PistonStructureResolver. resolves all cooperating chains in one pass so a
// structure hanging off several ropes moves as one. ropes are free and never sticky
public class PulleyStructureResolver {

    public record PulleyInfo(BlockPos pulleyPos, Block ropeBlock, Direction ropeHangDirection, boolean extending) {
        public Direction pushDirection() {
            return extending ? ropeHangDirection : ropeHangDirection.getOpposite();
        }
    }

    private final Level level;
    private final List<PulleyInfo> pulleys;
    private final Set<BlockPos> pulleyPositions;
    private final Direction pushDirection;
    private final boolean extending;
    private final int totalPushLimit;

    private final List<BlockPos> toPush = Lists.newArrayList();
    private final List<BlockPos> toDestroy = Lists.newArrayList();

    private final Set<BlockPos> ropePositions = new HashSet<>();
    //top rope of each retracting chain, eaten by the pulley instead of moved
    private final Set<BlockPos> consumedRopes = new HashSet<>();
    //only these earn the rope item
    private final Set<BlockPos> contributedPulleys = new HashSet<>();
    //placed instantly by the mover, there's no moved block to hang a phantom off
    private final Map<BlockPos, BlockState> directRopePlacements = new HashMap<>();

    public PulleyStructureResolver(Level level, List<PulleyInfo> pulleys) {
        if (pulleys.isEmpty()) throw new IllegalArgumentException("need at least one pulley");
        PulleyInfo first = pulleys.getFirst();
        for (PulleyInfo p : pulleys) {
            if (p.pushDirection() != first.pushDirection() || p.extending() != first.extending()) {
                throw new IllegalArgumentException("all pulleys must share push direction and extending mode");
            }
        }
        this.level = level;
        this.pulleys = pulleys;
        this.pulleyPositions = new HashSet<>();
        for (PulleyInfo p : pulleys) this.pulleyPositions.add(p.pulleyPos());
        this.pushDirection = first.pushDirection();
        this.extending = first.extending();
        this.totalPushLimit = pulleys.size() * CommonConfigs.Redstone.PULLEY_PULL_LIMIT.get();
    }

    private int blocksCountingTowardLimit() {
        int n = 0;
        for (BlockPos p : toPush) {
            if (!ropePositions.contains(p)) n++;
        }
        return n;
    }

    public boolean resolve() {
        toPush.clear();
        toDestroy.clear();
        ropePositions.clear();
        consumedRopes.clear();
        contributedPulleys.clear();
        directRopePlacements.clear();

        for (PulleyInfo pulley : pulleys) {
            if (!addChain(pulley)) return false;
        }
        //indexed, addBranchingBlocks appends to toPush while we walk it
        for (BlockPos pos : toPush) {
            if (ropePositions.contains(pos)) continue;
            if (SuppPlatformStuff.isSticky(level.getBlockState(pos)) && !addBranchingBlocks(pos)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasNothingToMove() {
        return toPush.isEmpty() && toDestroy.isEmpty() && directRopePlacements.isEmpty();
    }

    //walks one pulley's rope down to whatever it hangs on and adds that like a piston would
    private boolean addChain(PulleyInfo pulley) {
        int sizeBefore = toPush.size();
        Direction ropeDir = pulley.ropeHangDirection();
        BlockPos firstSlot = pulley.pulleyPos().relative(ropeDir);
        BlockPos walkPos = firstSlot;
        if (!extending) {
            //retracting needs a rope to eat, extending can start bare
            if (!RopeMover.isCorrectRope(pulley.ropeBlock(), level.getBlockState(firstSlot), ropeDir)) return true;
            consumedRopes.add(firstSlot);
            walkPos = firstSlot.relative(ropeDir);
        }
        while (RopeMover.isCorrectRope(pulley.ropeBlock(), level.getBlockState(walkPos), ropeDir)) {
            if (!toPush.contains(walkPos)) {
                toPush.add(walkPos);
                ropePositions.add(walkPos);
            }
            walkPos = walkPos.relative(ropeDir);
        }

        BlockPos anchorPos = walkPos;
        BlockState anchorState = level.getBlockState(anchorPos);
        boolean anchorIsAir = anchorState.isAir();
        if (anchorIsAir || pulleyPositions.contains(anchorPos)) {
            //nothing hangs off the rope, the column just shortens or grows by one
            boolean movedSomeRope = toPush.size() > sizeBefore;
            if (movedSomeRope) {
                contributedPulleys.add(pulley.pulleyPos());
            } else if (extending && anchorIsAir) {
                directRopePlacements.put(firstSlot, pulley.ropeBlock().defaultBlockState());
                contributedPulleys.add(pulley.pulleyPos());
            }
            return true;
        }
        //don't wind rope around something we can't move
        if (!isPullable(anchorState, level, anchorPos, pushDirection, false, ropeDir)) return false;

        if (!toPush.contains(anchorPos) && !addBlockLine(anchorPos, pushDirection)) return false;
        contributedPulleys.add(pulley.pulleyPos());
        return true;
    }

    //vanilla addBlockLine but pulley bodies are walls and ropes don't count toward the limit
    private boolean addBlockLine(BlockPos originPos, Direction approachDir) {
        BlockState currentState = this.level.getBlockState(originPos);

        if (currentState.isAir()) return true;
        if (!isPullable(currentState, this.level, originPos, this.pushDirection, false, approachDir))
            return true;
        if (this.pulleyPositions.contains(originPos)) return true;
        if (this.consumedRopes.contains(originPos)) return true;
        if (this.toPush.contains(originPos)) return true;

        int trailingCount = 1;
        if (this.blocksCountingTowardLimit() + trailingCount > this.totalPushLimit) return false;

        BlockState prevTrailingState;
        while (SuppPlatformStuff.isSticky(currentState)) {
            BlockPos trailingPos = originPos.relative(this.pushDirection.getOpposite(), trailingCount);
            prevTrailingState = currentState;
            currentState = this.level.getBlockState(trailingPos);
            if (currentState.isAir()
                    || !SuppPlatformStuff.canStickToEachOther(prevTrailingState, currentState)
                    || !isPullable(currentState, this.level, trailingPos, this.pushDirection, false, this.pushDirection.getOpposite())
                    || this.pulleyPositions.contains(trailingPos)
                    //else a sticky anchor would drag the rope above it along
                    || this.ropePositions.contains(trailingPos)
                    || this.consumedRopes.contains(trailingPos)) {
                break;
            }
            trailingCount++;
            if (this.blocksCountingTowardLimit() + trailingCount > this.totalPushLimit) return false;
        }

        int addedToThisLine = 0;
        for (int i1 = trailingCount - 1; i1 >= 0; i1--) {
            this.toPush.add(originPos.relative(this.pushDirection.getOpposite(), i1));
            addedToThisLine++;
        }

        int forwardScanStep = 1;
        while (true) {
            BlockPos forwardPos = originPos.relative(this.pushDirection, forwardScanStep);

            //reached a pulley mouth
            if (this.consumedRopes.contains(forwardPos)) return true;

            int collisionIndex = this.toPush.indexOf(forwardPos);
            if (collisionIndex > -1) {
                this.reorderListAtCollision(addedToThisLine, collisionIndex);
                for (int k = 0; k <= collisionIndex + addedToThisLine; k++) {
                    BlockPos mergedPos = this.toPush.get(k);
                    if (this.ropePositions.contains(mergedPos)) continue;
                    if (SuppPlatformStuff.isSticky(this.level.getBlockState(mergedPos)) && !this.addBranchingBlocks(mergedPos)) {
                        return false;
                    }
                }
                return true;
            }

            currentState = this.level.getBlockState(forwardPos);
            if (currentState.isAir()) return true;

            if (!isPullable(currentState, this.level, forwardPos, this.pushDirection, true, this.pushDirection)
                    || this.pulleyPositions.contains(forwardPos)) {
                return false;
            }

            if (currentState.getPistonPushReaction() == PushReaction.DESTROY) {
                this.toDestroy.add(forwardPos);
                return true;
            }

            if (this.blocksCountingTowardLimit() >= this.totalPushLimit) return false;

            this.toPush.add(forwardPos);
            addedToThisLine++;
            forwardScanStep++;
        }
    }

    private void reorderListAtCollision(int newBlockCount, int collisionIndex) {
        List<BlockPos> beforeCollision = new ArrayList<>(this.toPush.subList(0, collisionIndex));
        List<BlockPos> newlyAdded = new ArrayList<>(this.toPush.subList(this.toPush.size() - newBlockCount, this.toPush.size()));
        List<BlockPos> afterCollisionOld = new ArrayList<>(this.toPush.subList(collisionIndex, this.toPush.size() - newBlockCount));
        this.toPush.clear();
        this.toPush.addAll(beforeCollision);
        this.toPush.addAll(newlyAdded);
        this.toPush.addAll(afterCollisionOld);
    }

    private boolean addBranchingBlocks(BlockPos fromPos) {
        BlockState fromState = this.level.getBlockState(fromPos);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != this.pushDirection.getAxis()) {
                BlockPos neighborPos = fromPos.relative(direction);
                BlockState neighborState = this.level.getBlockState(neighborPos);
                if (SuppPlatformStuff.canStickToEachOther(neighborState, fromState)
                        && !this.addBlockLine(neighborPos, direction)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Direction getPushDirection() {
        return this.pushDirection;
    }

    public List<BlockPos> getToPush() {
        return this.toPush;
    }

    public List<BlockPos> getToDestroy() {
        return this.toDestroy;
    }

    public Set<BlockPos> getConsumedRopes() {
        return this.consumedRopes;
    }

    //extend only. the mover animates these as rope coming out of the pulley
    public Map<BlockPos, BlockState> getExtendingPhantomSources() {
        Map<BlockPos, BlockState> result = new HashMap<>();
        if (!extending) return result;
        for (PulleyInfo p : pulleys) {
            BlockPos firstSlot = p.pulleyPos().relative(p.ropeHangDirection());
            if (toPush.contains(firstSlot)) {
                result.put(firstSlot, p.ropeBlock().defaultBlockState());
            }
        }
        return result;
    }

    public Set<BlockPos> getContributedPulleys() {
        return this.contributedPulleys;
    }

    public Map<BlockPos, BlockState> getDirectRopePlacements() {
        return this.directRopePlacements;
    }

    //piston rules plus the rope blacklist, for stuff like doors whose other half wouldn't follow
    private static boolean isPullable(BlockState state, Level level, BlockPos pos,
                                      Direction movementDirection, boolean allowDestroy,
                                      Direction pulleyFacing) {
        if (state.is(ModTags.ROPE_PUSH_BLACKLIST)) return false;
        return PistonMovementHelper.isPushableByOurMovers(state, level, pos, movementDirection,
                allowDestroy, pulleyFacing);
    }
}
