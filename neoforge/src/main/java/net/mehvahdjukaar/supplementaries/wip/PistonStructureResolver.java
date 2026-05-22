package net.mehvahdjukaar.supplementaries.wip;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;

/**
 * Determines which blocks a piston will move (toPush) or destroy (toDestroy) when it activates.
 * <p>
 * toPush is ordered front-to-back: index 0 is the block closest to empty space (the "leading"
 * block), and the last index is the block adjacent to the piston face. Vanilla's
 * PistonBlockEntity.moveBlocks() iterates it in REVERSE so back blocks are placed first,
 * making room for the blocks behind them — like pulling a train from the front.
 */
public class PistonStructureResolver {
    /**
     * Maximum number of blocks one piston can push.
     */
    public static final int MAX_PUSH_DEPTH = 12;

    private final Level level;
    private final BlockPos pistonPos;
    private final boolean extending;
    /**
     * First block position the piston interacts with:
     * extending  → block directly in front of the piston face (pistonPos + pistonDir)
     * retracting → block two steps out (pistonPos + pistonDir*2), because the extended
     * head occupies the first step
     */
    private final BlockPos startPos;
    /**
     * Direction blocks will physically move:
     * extending  → same as pistonDirection (away from piston)
     * retracting → opposite of pistonDirection (toward piston)
     */
    private final Direction pushDirection;
    /**
     * All block positions that will be moved. Front-to-back order (see class javadoc).
     */
    private final List<BlockPos> toPush = Lists.newArrayList();
    /**
     * Blocks that will be destroyed instead of pushed (e.g. tall grass, lava).
     */
    private final List<BlockPos> toDestroy = Lists.newArrayList();
    /**
     * The direction the piston block faces (its "output" side).
     */
    private final Direction pistonDirection;

    public PistonStructureResolver(Level level, BlockPos pistonPos, Direction pistonDirection, boolean extending) {
        this.level = level;
        this.pistonPos = pistonPos;
        this.pistonDirection = pistonDirection;
        this.extending = extending;
        if (extending) {
            this.pushDirection = pistonDirection;
            this.startPos = pistonPos.relative(pistonDirection);
        } else {
            this.pushDirection = pistonDirection.getOpposite();
            this.startPos = pistonPos.relative(pistonDirection, 2);
        }
    }

    /**
     * Populates toPush and toDestroy.
     *
     * @return true if the piston can legally activate; false if an immovable block blocks it.
     */
    public boolean resolve() {
        this.toPush.clear();
        this.toDestroy.clear();

        BlockState startState = this.level.getBlockState(this.startPos);

        if (!PistonBaseBlock.isPushable(startState, this.level, this.startPos, this.pushDirection, false, this.pistonDirection)) {
            if (this.extending && startState.getPistonPushReaction() == PushReaction.DESTROY) {
                this.toDestroy.add(this.startPos);
                return true;
            } else {
                return false; // immovable block (e.g. obsidian) directly in the way
            }
        } else if (!this.addBlockLine(this.startPos, this.pushDirection)) {
            return false;
        } else {
            // For every sticky block already queued, drag in any laterally-adjacent stickies.
            for (int i = 0; i < this.toPush.size(); i++) {
                BlockPos blockPos = this.toPush.get(i);
                if (this.level.getBlockState(blockPos).isStickyBlock() && !this.addBranchingBlocks(blockPos)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Builds the push chain starting at originPos in three phases:
     * 1. Backward scan: follow sticky links against pushDirection to find the "tail" —
     * blocks clinging to originPos that must come along for the ride.
     * 2. Tail insertion: add the tail to toPush in front-to-back order (furthest first).
     * 3. Forward scan: walk pushDirection until air, an obstacle, or a block already queued.
     *
     * @param originPos   block to start from
     * @param approachDir direction we arrived at originPos from; passed to isPushable so it can
     *                    reject blocks that are only sticky on certain faces
     * @return false if the push is impossible (limit exceeded or an immovable block is in the way)
     */
    private boolean addBlockLine(BlockPos originPos, Direction approachDir) {
        BlockState currentState = this.level.getBlockState(originPos);

        if (currentState.isAir()) return true;
        if (!PistonBaseBlock.isPushable(currentState, this.level, originPos, this.pushDirection, false, approachDir))
            return true;
        if (originPos.equals(this.pistonPos)) return true;  // never push the piston itself
        if (this.toPush.contains(originPos)) return true;   // already queued via another chain

        // ── Phase 1: backward scan ────────────────────────────────────────────────
        // Walk backwards (against pushDirection) as long as adjacent blocks are mutually sticky.
        // trailingCount starts at 1: originPos itself always counts toward the budget.
        int trailingCount = 1;
        if (trailingCount + this.toPush.size() > MAX_PUSH_DEPTH) return false;

        BlockState prevTrailingState; // state of the block we just left in the backward walk
        while (currentState.isStickyBlock()) {
            BlockPos trailingPos = originPos.relative(this.pushDirection.getOpposite(), trailingCount);
            prevTrailingState = currentState;
            currentState = this.level.getBlockState(trailingPos);
            // Stop if chain breaks: air, one-sided stickiness, not pushable, or we reached the piston.
            if (currentState.isAir()
                    || !(prevTrailingState.canStickTo(currentState) && currentState.canStickTo(prevTrailingState))
                    || !PistonBaseBlock.isPushable(currentState, this.level, trailingPos, this.pushDirection, false, this.pushDirection.getOpposite())
                    || trailingPos.equals(this.pistonPos)) {
                break;
            }
            if (++trailingCount + this.toPush.size() > MAX_PUSH_DEPTH) return false;
        }

        // ── Phase 2: insert the tail into toPush, front-to-back ──────────────────
        // i1 counts down so the block furthest from the piston (at -trailingCount+1 offset)
        // is added first, keeping toPush in front-to-back order.
        int addedToThisLine = 0;
        for (int i1 = trailingCount - 1; i1 >= 0; i1--) {
            this.toPush.add(originPos.relative(this.pushDirection.getOpposite(), i1));
            addedToThisLine++;
        }

        // ── Phase 3: forward scan ─────────────────────────────────────────────────
        int forwardScanStep = 1;
        while (true) {
            BlockPos forwardPos = originPos.relative(this.pushDirection, forwardScanStep);

            int collisionIndex = this.toPush.indexOf(forwardPos);
            if (collisionIndex > -1) {
                // forwardPos is already in toPush — reached via a different addBlockLine call
                // (typically a sticky branch looping back). Merge and re-sort to preserve order.
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
                    || forwardPos.equals(this.pistonPos)) {
                return false; // blocked
            }

            if (currentState.getPistonPushReaction() == PushReaction.DESTROY) {
                this.toDestroy.add(forwardPos);
                return true;
            }

            if (this.toPush.size() >= MAX_PUSH_DEPTH) return false;

            this.toPush.add(forwardPos);
            addedToThisLine++;
            forwardScanStep++;
        }
    }

    /**
     * Called when the forward scan hits a block already present in toPush (a "collision"),
     * meaning two independent chains share a common end-point due to sticky connections.
     * <p>
     * Before this call, toPush looks like:
     * [ pre-collision... | post-collision-old... | newly-added... ]
     * ^ collisionIndex          ^ size - newBlockCount
     * <p>
     * After this call:
     * [ pre-collision... | newly-added... | post-collision-old... ]
     * <p>
     * The newly-added blocks are physically behind (against pushDirection) the collision point,
     * so they must be pushed after the pre-collision blocks but before the old post-collision
     * segment. Vanilla's reverse iteration then moves them in the correct sequence.
     */
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

    /**
     * For a sticky block at fromPos, checks all four perpendicular directions (not on the
     * push axis) for blocks that mutually stick to it, and queues them via addBlockLine.
     * Both blocks must agree to stick (canStickTo is not always symmetric — e.g. honey vs slime).
     */
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