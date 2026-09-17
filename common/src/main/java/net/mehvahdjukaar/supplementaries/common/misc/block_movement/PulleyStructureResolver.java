package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.util.*;

// pulley version of PistonStructureResolver. resolves all cooperating rope columns in one pass so a
// structure hanging off several ropes moves as one. ropes are free and never sticky
public class PulleyStructureResolver {

    public record RopeColumn(BlockPos pulleyPos, Block ropeBlock, Direction ropeHangDirection, boolean extending) {
        public Direction pushDirection() {
            return extending ? ropeHangDirection : ropeHangDirection.getOpposite();
        }
    }

    private final Level level;
    private final List<RopeColumn> pulleys;
    private final Set<BlockPos> pulleyPositions;
    private final Direction pushDirection;
    private final boolean extending;
    private final int totalPushLimit;

    private final List<BlockPos> toPush = Lists.newArrayList();
    private final List<BlockPos> toDestroy = Lists.newArrayList();

    private final Set<BlockPos> ropePositions = new HashSet<>();
    private final Set<BlockPos> ropesRetractedIntoPulleys = new HashSet<>();
    private final Set<BlockPos> pulleysWhoseColumnMoves = new HashSet<>();
    private final Map<BlockPos, BlockState> ropesPlacedWithoutAnimation = new HashMap<>();

    public PulleyStructureResolver(Level level, List<RopeColumn> pulleys) {
        if (pulleys.isEmpty()) throw new IllegalArgumentException("need at least one pulley");
        RopeColumn first = pulleys.getFirst();
        for (RopeColumn p : pulleys) {
            if (p.pushDirection() != first.pushDirection() || p.extending() != first.extending()) {
                throw new IllegalArgumentException("all pulleys must share push direction and extending mode");
            }
        }
        this.level = level;
        this.pulleys = pulleys;
        this.pulleyPositions = new HashSet<>();
        for (RopeColumn p : pulleys) this.pulleyPositions.add(p.pulleyPos());
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
        ropesRetractedIntoPulleys.clear();
        pulleysWhoseColumnMoves.clear();
        ropesPlacedWithoutAnimation.clear();

        for (RopeColumn pulley : pulleys) {
            if (!addRopeColumnAndWhatHangsOnIt(pulley)) return false;
        }
        //indexed on purpose, addBranchingBlocks appends to toPush while we walk it
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < toPush.size(); i++) {
            BlockPos pos = toPush.get(i);
            if (ropePositions.contains(pos)) continue;
            if (BlockMovementHelper.isSticky(level.getBlockState(pos)) && !addBranchingBlocks(pos)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasNothingToMove() {
        return toPush.isEmpty() && toDestroy.isEmpty() && ropesPlacedWithoutAnimation.isEmpty();
    }

    private boolean addRopeColumnAndWhatHangsOnIt(RopeColumn pulley) {
        int sizeBefore = toPush.size();
        Direction ropeDir = pulley.ropeHangDirection();
        BlockPos firstSlot = pulley.pulleyPos().relative(ropeDir);
        BlockPos walkPos = firstSlot;
        if (!extending) {
            boolean hasRopeToRetract = InstantPulleyMover.isCorrectRope(pulley.ropeBlock(), level.getBlockState(firstSlot), ropeDir);
            if (!hasRopeToRetract) return true;
            ropesRetractedIntoPulleys.add(firstSlot);
            walkPos = firstSlot.relative(ropeDir);
        }
        while (InstantPulleyMover.isCorrectRope(pulley.ropeBlock(), level.getBlockState(walkPos), ropeDir)) {
            if (!toPush.contains(walkPos)) {
                toPush.add(walkPos);
                ropePositions.add(walkPos);
            }
            walkPos = walkPos.relative(ropeDir);
        }

        BlockPos anchorPos = walkPos;
        BlockState anchorState = level.getBlockState(anchorPos);
        boolean anchorIsAir = anchorState.isAir();
        boolean nothingHangsOnRope = anchorIsAir || pulleyPositions.contains(anchorPos);
        if (nothingHangsOnRope) {
            boolean movedSomeRope = toPush.size() > sizeBefore;
            if (movedSomeRope) {
                pulleysWhoseColumnMoves.add(pulley.pulleyPos());
            } else if (extending && anchorIsAir) {
                ropesPlacedWithoutAnimation.put(firstSlot, pulley.ropeBlock().defaultBlockState());
                pulleysWhoseColumnMoves.add(pulley.pulleyPos());
            }
            return true;
        }
        if (!isPullable(anchorState, level, anchorPos, pushDirection, false, ropeDir)) return false;

        if (!toPush.contains(anchorPos) && !addBlockLine(anchorPos, pushDirection)) return false;
        pulleysWhoseColumnMoves.add(pulley.pulleyPos());
        return true;
    }

    //vanilla addBlockLine but pulley bodies are walls and ropes don't count toward the limit
    private boolean addBlockLine(BlockPos originPos, Direction approachDir) {
        BlockState currentState = this.level.getBlockState(originPos);

        if (currentState.isAir()) return true;
        if (!isPullable(currentState, this.level, originPos, this.pushDirection, false, approachDir))
            return true;
        if (this.pulleyPositions.contains(originPos)) return true;
        if (this.ropesRetractedIntoPulleys.contains(originPos)) return true;
        if (this.toPush.contains(originPos)) return true;

        int trailingCount = 1;
        if (this.blocksCountingTowardLimit() + trailingCount > this.totalPushLimit) return false;

        BlockState prevTrailingState;
        while (BlockMovementHelper.isSticky(currentState)) {
            BlockPos trailingPos = originPos.relative(this.pushDirection.getOpposite(), trailingCount);
            prevTrailingState = currentState;
            currentState = this.level.getBlockState(trailingPos);
            if (currentState.isAir()
                    || !BlockMovementHelper.canStickToEachOther(prevTrailingState, currentState, this.pushDirection.getOpposite())
                    || !isPullable(currentState, this.level, trailingPos, this.pushDirection, false, this.pushDirection.getOpposite())
                    || this.pulleyPositions.contains(trailingPos)
                    //else a sticky anchor would drag the rope above it along
                    || this.ropePositions.contains(trailingPos)
                    || this.ropesRetractedIntoPulleys.contains(trailingPos)) {
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

            boolean reachedPulleyMouth = this.ropesRetractedIntoPulleys.contains(forwardPos);
            if (reachedPulleyMouth) return true;

            int collisionIndex = this.toPush.indexOf(forwardPos);
            if (collisionIndex > -1) {
                this.reorderListAtCollision(addedToThisLine, collisionIndex);
                for (int k = 0; k <= collisionIndex + addedToThisLine; k++) {
                    BlockPos mergedPos = this.toPush.get(k);
                    if (this.ropePositions.contains(mergedPos)) continue;
                    if (BlockMovementHelper.isSticky(this.level.getBlockState(mergedPos)) && !this.addBranchingBlocks(mergedPos)) {
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
                if (BlockMovementHelper.canStickToEachOther(neighborState, fromState, direction.getOpposite())
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

    public Set<BlockPos> getRopesRetractedIntoPulleys() {
        return this.ropesRetractedIntoPulleys;
    }

    public Map<BlockPos, BlockState> getRopesEmergingFromPulleys() {
        Map<BlockPos, BlockState> emergingRopes = new HashMap<>();
        if (!extending) return emergingRopes;
        for (RopeColumn p : pulleys) {
            BlockPos firstSlot = p.pulleyPos().relative(p.ropeHangDirection());
            if (toPush.contains(firstSlot)) {
                emergingRopes.put(firstSlot, p.ropeBlock().defaultBlockState());
            }
        }
        return emergingRopes;
    }

    public Set<BlockPos> getPulleysWhoseColumnMoves() {
        return this.pulleysWhoseColumnMoves;
    }

    public Map<BlockPos, BlockState> getRopesPlacedWithoutAnimation() {
        return this.ropesPlacedWithoutAnimation;
    }

    //piston rules plus the rope blacklist, for stuff like doors whose other half wouldn't follow
    private static boolean isPullable(BlockState state, Level level, BlockPos pos,
                                      Direction movementDirection, boolean allowDestroy,
                                      Direction approachDir) {
        if (state.is(ModTags.ROPE_PUSH_BLACKLIST)) return false;
        return BlockMovementHelper.isPushableByOurMovers(state, level, pos, movementDirection,
                allowDestroy, approachDir);
    }
}
