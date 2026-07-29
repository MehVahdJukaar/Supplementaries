package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;

import java.util.*;

/**
 * Pulley-side equivalent of vanilla {@code PistonStructureResolver}. Takes one
 * {@link PulleyInfo} per cooperating pulley and resolves all their chains in a single pass, so
 * a structure bridged across several ropes moves as one unit and the pulleys pool their budget.
 * <p>
 * Two operations, picked by {@link PulleyInfo#extending()}:
 * <ul>
 *   <li><b>Retract</b> ({@code extending=false}): chain moves toward the pulley. The rope
 *   directly adjacent to the pulley is <i>consumed</i> (item +1) and not animated. Ropes
 *   between that consumed top rope and the anchor go into {@code toPush} (they shift one
 *   slot up via moving-block entities) but don't count against the push budget.</li>
 *   <li><b>Extend</b> ({@code extending=true}): existing ropes stay put. Only the anchor,
 *   plus any sticky-branched blocks attached to it, moves one slot away. A new rope is
 *   placed in the anchor's vacated slot (handled by the caller, not here).</li>
 * </ul>
 * In either mode, stickiness does not propagate through ropes: a slime laterally adjacent to
 * a rope segment is never dragged in. Sticky branching only fires from the anchor and
 * anything in its sticky chain.
 */
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
    // Subset of toPush that are rope segments: not counted toward budget, never sticky-branched.
    private final Set<BlockPos> ropePositions = new HashSet<>();
    // Topmost-rope positions, one per pulley: the rope slot directly adjacent to the pulley.
    // These are consumed into the pulley (overwritten by the next rope shifting up) rather than
    // being moved themselves. The forward scan treats them as a terminator, so a chain that walks
    // into one ends successfully instead of trying to push past into the pulley wall behind it.
    private final Set<BlockPos> consumedRopes = new HashSet<>();
    // Pulleys whose chain actually added at least one rope or anchor to toPush. The caller uses
    // this to decide which pulleys deserve a +1 item this step: a pulley whose chain was already
    // exhausted (e.g. single rope with no anchor below) sits in the cooperation group but doesn't
    // earn a rope this step.
    private final Set<BlockPos> contributedPulleys = new HashSet<>();
    // Extend-into-open-air placements, firstSlot -> rope state. A pulley lowering into pure air
    // has no rope column and no anchor to push, so there's no moved block to carry the usual
    // extend phantom. The mover instead places these rope states directly (instantly, no
    // animation). Empty in retract mode and whenever a real push/phantom already grows the chain.
    private final Map<BlockPos, BlockState> directRopePlacements = new HashMap<>();

    public PulleyStructureResolver(Level level, List<PulleyInfo> pulleys) {
        if (pulleys.isEmpty()) throw new IllegalArgumentException("need at least one pulley");
        PulleyInfo first = pulleys.getFirst();
        Direction sharedPush = first.pushDirection();
        boolean sharedExtending = first.extending();
        for (PulleyInfo p : pulleys) {
            if (p.pushDirection() != sharedPush || p.extending() != sharedExtending) {
                throw new IllegalArgumentException("all pulleys must share push direction and extending mode");
            }
        }
        this.level = level;
        this.pulleys = pulleys;
        this.pulleyPositions = new HashSet<>();
        for (PulleyInfo p : pulleys) this.pulleyPositions.add(p.pulleyPos());
        this.pushDirection = sharedPush;
        this.extending = sharedExtending;
        this.totalPushLimit = pulleys.size() * CommonConfigs.Redstone.PULLEY_PULL_LIMIT.get();
    }

    // Non-rope blocks already queued: the figure compared against the budget. Counted iteratively
    // rather than as toPush.size() - ropePositions.size() because in extend mode ropePositions
    // holds chain ropes that are NOT in toPush, so the subtraction would underflow.
    private int budgetUsage() {
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
            int contributionMark = toPush.size();
            Direction ropeDir = pulley.ropeHangDirection();
            BlockPos firstSlot = pulley.pulleyPos().relative(ropeDir);
            BlockState firstState = level.getBlockState(firstSlot);

            // Retracting needs a rope at firstSlot to consume; extending can push an anchor
            // that sits directly below with no rope yet (phantom places the new rope on finish).
            if (!extending && !RopeMover.isCorrectRope(pulley.ropeBlock(), firstState, ropeDir)) {
                continue;
            }

            // Both modes shift the entire rope column by one slot: a symmetric move.
            // RETRACT: chain moves toward the pulley. The topmost rope (firstSlot) is marked
            // consumed so it doesn't get a MOVING_PISTON entry; the rope below it slides up to
            // overwrite that slot. The forward scan treats consumed slots as a soft terminator
            // so the chain ends cleanly at the pulley wall.
            // EXTEND: chain moves away from the pulley. Every rope animates one slot in the
            // push direction; the topmost slot (firstSlot) ends up empty and is then filled by
            // {@code serverFinaliseExtend} with a fresh rope. The bottom rope ends up where the
            // anchor was, and the anchor slides further down (handled by addBlockLine).
            BlockPos walkPos;
            if (!extending) {
                consumedRopes.add(firstSlot);
                walkPos = firstSlot.relative(ropeDir);
            } else {
                walkPos = firstSlot;
            }
            while (true) {
                BlockState state = level.getBlockState(walkPos);
                if (!RopeMover.isCorrectRope(pulley.ropeBlock(), state, ropeDir)) break;
                if (!toPush.contains(walkPos)) {
                    toPush.add(walkPos);
                    ropePositions.add(walkPos);
                }
                walkPos = walkPos.relative(ropeDir);
            }

            // walkPos is now the anchor position (first non-rope reached). If empty, the ropes
            // simply shorten by one: no anchor to attach a sticky chain to.
            BlockState anchorState = level.getBlockState(walkPos);
            boolean anchorIsAir = anchorState.isAir();
            boolean anchorIsPulley = pulleyPositions.contains(walkPos);
            boolean anchorPullable = !anchorIsAir && !anchorIsPulley
                    && isPullable(anchorState, level, walkPos, pushDirection, false, ropeDir);

            if (!anchorPullable) {
                if (anchorIsAir || anchorIsPulley) {
                    // No anchor at all (just hanging rope), or the chain runs into another
                    // pulley body: the rope chain still shortens by one if we walked any
                    // intermediate rope segments. Mark contribution if so.
                    if (toPush.size() > contributionMark) {
                        contributedPulleys.add(pulley.pulleyPos());
                    } else if (extending && anchorIsAir) {
                        // Pure open air below the pulley (firstSlot itself is air): nothing to push
                        // and no column to grow, but lowering must still drop a fresh rope into
                        // firstSlot. There's no moved block to carry a phantom rope here, so queue a
                        // direct placement and count this pulley as contributing so the caller spends
                        // a rope from its spool. (firstSlot == walkPos in this branch.)
                        directRopePlacements.put(firstSlot, pulley.ropeBlock().defaultBlockState());
                        contributedPulleys.add(pulley.pulleyPos());
                    }
                    continue;
                }
                // Anchor exists but is immovable (obsidian, DESTROY-reaction block, etc.).
                // Fail the whole resolve so the player gets clear "nothing happens" feedback
                // rather than the pulley silently winding rope and leaving the anchor stuck.
                return false;
            }

            if (toPush.contains(walkPos)) {
                // Anchor already added by another pulley's sticky chain. We still count as
                // contributing, since our column shifts via the shared structure.
                contributedPulleys.add(pulley.pulleyPos());
                continue;
            }
            if (!addBlockLine(walkPos, pushDirection)) return false;
            if (toPush.size() > contributionMark) {
                contributedPulleys.add(pulley.pulleyPos());
            }
        }
        // Sticky branching pass, skipping ropes (no slime-via-rope chains).
        // MUST be an indexed loop: addBranchingBlocks -> addBlockLine mutates toPush, and we
        // want newly-added blocks (e.g. slime laterally stuck to the anchor) to also be
        // visited by this pass. An enhanced-for / iterator throws ConcurrentModificationException
        // the moment branching adds anything. Vanilla's PistonStructureResolver uses the same
        // indexed pattern for the same reason.
        for (BlockPos pos : toPush) {
            if (ropePositions.contains(pos)) continue;
            if (SuppPlatformStuff.isSticky(level.getBlockState(pos)) && !addBranchingBlocks(pos)) {
                return false;
            }
        }
        return true;
    }

    // Vanilla addBlockLine, with two modifications: the piston-position guard uses pulleyPositions
    // (any cooperating pulley is a wall), and the push-limit checks use budgetUsage() instead of
    // raw toPush.size() so rope segments don't consume budget.
    private boolean addBlockLine(BlockPos originPos, Direction approachDir) {
        BlockState currentState = this.level.getBlockState(originPos);

        if (currentState.isAir()) return true;
        if (!isPullable(currentState, this.level, originPos, this.pushDirection, false, approachDir))
            return true;
        if (this.pulleyPositions.contains(originPos)) return true;
        if (this.consumedRopes.contains(originPos)) return true;
        if (this.toPush.contains(originPos)) return true;

        int trailingCount = 1;
        if (this.budgetUsage() + trailingCount > this.totalPushLimit) return false;

        BlockState prevTrailingState;
        while (SuppPlatformStuff.isSticky(currentState)) {
            BlockPos trailingPos = originPos.relative(this.pushDirection.getOpposite(), trailingCount);
            prevTrailingState = currentState;
            currentState = this.level.getBlockState(trailingPos);
            if (currentState.isAir()
                    || !SuppPlatformStuff.canStickToEachOther(prevTrailingState, currentState)
                    || !isPullable(currentState, this.level, trailingPos, this.pushDirection, false, this.pushDirection.getOpposite())
                    || this.pulleyPositions.contains(trailingPos)
                    // Stop at any chain-rope position. Without this, a sticky anchor (e.g. slime
                    // during extend) would consider the rope above it sticky-attached via the
                    // "either side sticky" rule in canStickToEachOther and drag it along.
                    || this.ropePositions.contains(trailingPos)
                    || this.consumedRopes.contains(trailingPos)) {
                break;
            }
            trailingCount++;
            if (this.budgetUsage() + trailingCount > this.totalPushLimit) return false;
        }

        int addedToThisLine = 0;
        for (int i1 = trailingCount - 1; i1 >= 0; i1--) {
            this.toPush.add(originPos.relative(this.pushDirection.getOpposite(), i1));
            addedToThisLine++;
        }

        int forwardScanStep = 1;
        while (true) {
            BlockPos forwardPos = originPos.relative(this.pushDirection, forwardScanStep);

            // Chain reached the pulley's mouth, so terminate successfully. The consumed rope
            // will be overwritten by whichever block in toPush has it as its destination.
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

            if (this.budgetUsage() >= this.totalPushLimit) return false;

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

    public Set<BlockPos> getRopePositions() {
        return this.ropePositions;
    }

    public Set<BlockPos> getConsumedRopes() {
        return this.consumedRopes;
    }

    /**
     * For extend mode: returns the firstSlot positions whose rope chain contributed to the
     * move, mapped to the rope state that should appear there. The mover uses this to flag
     * the topmost MOVING_PULLEY in each chain as carrying a "trailing" phantom, a new rope
     * animating from inside the pulley down into firstSlot. Empty in retract mode.
     */
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

    /**
     * Extend-only: {@code firstSlot -> rope state} for pulleys lowering into open air, where the
     * rope must be placed directly (no moved block, no phantom animation). See
     * {@link #directRopePlacements}. Empty in retract mode.
     */
    public Map<BlockPos, BlockState> getDirectRopePlacements() {
        return this.directRopePlacements;
    }

    /**
     * Pulley-flavoured pushability check. Identical to vanilla {@code PistonBaseBlock.isPushable}
     * (including all NeoForge {@code IBlockExtension} hooks invoked by the state-level methods it
     * calls) except for one deliberate divergence: <b>block-entity-bearing blocks are not
     * automatically rejected</b>. Vanilla's final {@code return !state.hasBlockEntity()} is
     * dropped so that pulleys can pull blocks like chests or barrels, with NBT preserved
     * separately by the mover.
     * <p>
     * Blocks that genuinely shouldn't be moved (machines with anchored physics, blocks whose
     * BE references absolute world position, etc.) should override
     * {@code getPistonPushReaction} to return {@link PushReaction#BLOCK} as usual.
     */
    public static boolean isPullable(BlockState state, Level level, BlockPos pos,
                                     Direction movementDirection, boolean allowDestroy,
                                     Direction pulleyFacing) {
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() > level.getMaxBuildHeight() - 1
                || !level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (state.isAir()) return true;
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.RESPAWN_ANCHOR) || state.is(Blocks.REINFORCED_DEEPSLATE)) {
            return false;
        }
        if (movementDirection == Direction.DOWN && pos.getY() == level.getMinBuildHeight()) return false;
        if (movementDirection == Direction.UP && pos.getY() == level.getMaxBuildHeight() - 1) return false;
        if (!state.is(Blocks.PISTON) && !state.is(Blocks.STICKY_PISTON)) {
            if (state.getDestroySpeed(level, pos) == -1.0F) return false;
            switch (state.getPistonPushReaction()) {
                case BLOCK:
                    return false;
                case DESTROY:
                    return allowDestroy;
                case PUSH_ONLY:
                    return movementDirection == pulleyFacing;
            }
        } else if (state.getValue(BlockStateProperties.EXTENDED)) {
            return false;
        }
        // NOTE: vanilla returns !state.hasBlockEntity() here; we don't, see method javadoc.
        return true;
    }
}
