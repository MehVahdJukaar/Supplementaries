package net.mehvahdjukaar.supplementaries.wip;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extended piston structure resolver for cooperative multi-piston pushing.
 * <p>
 * When N pistons all extend on the same tick in the same direction and their push chains
 * touch the same structure, this resolver merges them into a single solve with a combined
 * push budget of N × MAX_PUSH_DEPTH_PER_PISTON (12 per piston in vanilla).
 * <p>
 * DESIGN DECISIONS:
 * <p>
 * Budget is global, not per-piston.
 * The totalPushLimit is shared across every addBlockLine call regardless of which piston
 * initiated it. This means two pistons can collectively push a single 24-block column,
 * not just two independent 12-block columns. Whichever piston's chain fills the budget
 * first "uses up" the shared capacity; once full, any additional blocks fail the push.
 * This is intentional: the feature models N pistons providing N×12 units of force, not
 * N pistons each independently capped at 12.
 * <p>
 * All piston positions are immovable anchors.
 * Both the forward scan and the backward sticky-tail scan treat every cooperating piston
 * as a wall. This prevents them from "pushing" each other and stops sticky tails from
 * wrapping back through a piston body. A piston position encountered in the forward scan
 * causes the push to fail (same as obsidian) — cooperating pistons must not be in the
 * direct push path of another cooperating piston.
 * <p>
 * Sticky deduplication is implicit.
 * If piston A's addBlockLine already queued piston B's startPos via sticky connections,
 * the resolve() loop silently skips B's call. No double-counting occurs.
 * <p>
 * All-or-nothing failure.
 * If any piston's startPos is immovably blocked, the whole cooperative push fails.
 * All pistons must be able to extend for the feature to make sense.
 * <p>
 * INTEGRATION NOTE:
 * The caller (typically a piston-activation event hook) is responsible for:
 * 1. Detecting that multiple pistons are activating in the same direction on the same tick.
 * 2. Grouping them and constructing a MultiPistonStructureResolver instead of per-piston resolvers.
 * 3. Using the combined toPush / toDestroy lists when scheduling block movement.
 * Each piston still generates its own BlockEvent so vanilla spawns their piston-head entities;
 * only the structure resolution step is shared.
 */
public class MultiPistonStructureResolver {
    public static final int MAX_PUSH_DEPTH_PER_PISTON = 12;

    private final Level level;
    /**
     * All cooperating piston positions. Stored as a Set for O(1) membership checks in the hot path.
     */
    private final Set<BlockPos> pistonPositions;
    private final boolean extending;
    /**
     * One start position per piston (order matches the input list).
     */
    private final List<BlockPos> startPositions;
    private final Direction pushDirection;
    private final Direction pistonDirection;
    /**
     * N × MAX_PUSH_DEPTH_PER_PISTON — the combined push budget shared by all chains.
     */
    private final int totalPushLimit;

    /**
     * All block positions that will be moved. Front-to-back order (see PistonStructureResolver).
     */
    private final List<BlockPos> toPush = Lists.newArrayList();
    /**
     * Blocks that will be destroyed instead of pushed.
     */
    private final List<BlockPos> toDestroy = Lists.newArrayList();

    /**
     * @param pistonPosList all cooperating piston positions; must be non-empty and all face
     *                      pistonDirection
     */
    public MultiPistonStructureResolver(Level level, List<BlockPos> pistonPosList,
                                        Direction pistonDirection, boolean extending) {
        if (pistonPosList.isEmpty()) throw new IllegalArgumentException("need at least one piston");
        this.level = level;
        this.pistonPositions = new HashSet<>(pistonPosList);
        this.pistonDirection = pistonDirection;
        this.extending = extending;
        this.totalPushLimit = pistonPosList.size() * MAX_PUSH_DEPTH_PER_PISTON;

        this.startPositions = new ArrayList<>(pistonPosList.size());
        if (extending) {
            this.pushDirection = pistonDirection;
            for (BlockPos pos : pistonPosList) this.startPositions.add(pos.relative(pistonDirection));
        } else {
            this.pushDirection = pistonDirection.getOpposite();
            // Extended piston head occupies pistonPos+1, so the block to pull is at pistonPos+2.
            for (BlockPos pos : pistonPosList) this.startPositions.add(pos.relative(pistonDirection, 2));
        }
    }

    /**
     * Convenience constructor for the common two-piston case.
     */
    public MultiPistonStructureResolver(Level level, BlockPos pistonPosA, BlockPos pistonPosB,
                                        Direction pistonDirection, boolean extending) {
        this(level, List.of(pistonPosA, pistonPosB), pistonDirection, extending);
    }

    public boolean resolve() {
        this.toPush.clear();
        this.toDestroy.clear();

        for (BlockPos startPos : this.startPositions) {
            // A previous piston's addBlockLine may have already queued this startPos via sticky
            // connections. If so, skip — its contribution is already in the shared toPush.
            if (this.toPush.contains(startPos)) continue;

            BlockState startState = this.level.getBlockState(startPos);

            if (!PistonBaseBlock.isPushable(startState, this.level, startPos, this.pushDirection, false, this.pistonDirection)) {
                if (this.extending && startState.getPistonPushReaction() == PushReaction.DESTROY) {
                    this.toDestroy.add(startPos);
                    // This piston destroys its first block; remaining pistons still contribute.
                } else {
                    // Immovable block in the way — the whole cooperative push fails.
                    return false;
                }
            } else if (!this.addBlockLine(startPos, this.pushDirection)) {
                return false;
            }
        }

        for (int i = 0; i < this.toPush.size(); i++) {
            BlockPos blockPos = this.toPush.get(i);
            if (this.level.getBlockState(blockPos).isStickyBlock() && !this.addBranchingBlocks(blockPos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Identical logic to PistonStructureResolver.addBlockLine with exactly two differences:
     * 1. Piston-position guard uses pistonPositions.contains() instead of a single equals().
     * 2. All three push-limit checks compare against totalPushLimit (N×12) instead of 12.
     * <p>
     * These two changes are the complete implementation of the cooperative-push feature.
     * Everything else — backward sticky-tail scanning, front-to-back insertion, forward
     * scanning, collision detection and reordering — is unchanged from vanilla.
     */
    private boolean addBlockLine(BlockPos originPos, Direction approachDir) {
        BlockState currentState = this.level.getBlockState(originPos);

        if (currentState.isAir()) return true;
        if (!PistonBaseBlock.isPushable(currentState, this.level, originPos, this.pushDirection, false, approachDir))
            return true;
        if (this.pistonPositions.contains(originPos)) return true; // [CHANGE 1] don't push any cooperating piston
        if (this.toPush.contains(originPos)) return true;

        int trailingCount = 1;
        if (trailingCount + this.toPush.size() > this.totalPushLimit) return false; // [CHANGE 2a]

        BlockState prevTrailingState;
        while (currentState.isStickyBlock()) {
            BlockPos trailingPos = originPos.relative(this.pushDirection.getOpposite(), trailingCount);
            prevTrailingState = currentState;
            currentState = this.level.getBlockState(trailingPos);
            if (currentState.isAir()
                    || !(prevTrailingState.canStickTo(currentState) && currentState.canStickTo(prevTrailingState))
                    || !PistonBaseBlock.isPushable(currentState, this.level, trailingPos, this.pushDirection, false, this.pushDirection.getOpposite())
                    || this.pistonPositions.contains(trailingPos)) { // [CHANGE 1] stop tail at any cooperating piston
                break;
            }
            if (++trailingCount + this.toPush.size() > this.totalPushLimit) return false; // [CHANGE 2b]
        }

        int addedToThisLine = 0;
        for (int i1 = trailingCount - 1; i1 >= 0; i1--) {
            this.toPush.add(originPos.relative(this.pushDirection.getOpposite(), i1));
            addedToThisLine++;
        }

        int forwardScanStep = 1;
        while (true) {
            BlockPos forwardPos = originPos.relative(this.pushDirection, forwardScanStep);

            int collisionIndex = this.toPush.indexOf(forwardPos);
            if (collisionIndex > -1) {
                this.reorderListAtCollision(addedToThisLine, collisionIndex);
                for (int k = 0; k <= collisionIndex + addedToThisLine; k++) {
                    BlockPos mergedPos = this.toPush.get(k);
                    if (this.level.getBlockState(mergedPos).isStickyBlock() && !this.addBranchingBlocks(mergedPos)) {
                        return false;
                    }
                }
                return true;
            }

            currentState = this.level.getBlockState(forwardPos);
            if (currentState.isAir()) return true;

            if (!PistonBaseBlock.isPushable(currentState, this.level, forwardPos, this.pushDirection, true, this.pushDirection)
                    || this.pistonPositions.contains(forwardPos)) { // [CHANGE 1] cooperating piston in the push path blocks it
                return false;
            }

            if (currentState.getPistonPushReaction() == PushReaction.DESTROY) {
                this.toDestroy.add(forwardPos);
                return true;
            }

            if (this.toPush.size() >= this.totalPushLimit) return false; // [CHANGE 2c]

            this.toPush.add(forwardPos);
            addedToThisLine++;
            forwardScanStep++;
        }
    }

    private void reorderListAtCollision(int newBlockCount, int collisionIndex) {
        List<BlockPos> beforeCollision = Lists.newArrayList();
        List<BlockPos> newlyAdded = Lists.newArrayList();
        List<BlockPos> afterCollisionOld = Lists.newArrayList();
        beforeCollision.addAll(this.toPush.subList(0, collisionIndex));
        newlyAdded.addAll(this.toPush.subList(this.toPush.size() - newBlockCount, this.toPush.size()));
        afterCollisionOld.addAll(this.toPush.subList(collisionIndex, this.toPush.size() - newBlockCount));
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
                if (neighborState.canStickTo(fromState) && fromState.canStickTo(neighborState)
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
}