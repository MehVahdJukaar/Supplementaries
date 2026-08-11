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

// Pulley-side equivalent of vanilla PistonStructureResolver. Takes one PulleyInfo per cooperating
// pulley and resolves all their chains in one pass, so a structure bridged across several ropes
// moves as a unit and the pulleys pool their push budget.
// Retracting, the chain moves toward the pulley and the rope adjacent to it is consumed (item +1)
// instead of animated. Extending, the ropes shift one slot away and a fresh rope fills the vacated
// top slot. Either way ropes don't count against the budget, and stickiness never propagates through
// them: sticky branching only fires from the anchor and its own sticky chain.
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
    // Topmost rope of each chain: consumed into the pulley (overwritten by the rope shifting up)
    // rather than moved. The forward scan treats these as a terminator, so a chain walking into one
    // ends successfully instead of pushing into the pulley wall behind it.
    private final Set<BlockPos> consumedRopes = new HashSet<>();
    // Pulleys whose chain added at least one rope or anchor. Only these earn a +1 item this step;
    // a pulley with an already exhausted chain sits in the group without contributing.
    private final Set<BlockPos> contributedPulleys = new HashSet<>();
    // Extend-into-open-air placements, firstSlot -> rope state. Lowering into pure air has no moved
    // block to carry the usual phantom rope, so the mover places these instantly instead.
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

    // Non-rope blocks already queued, the figure compared against the budget. Counted iteratively
    // rather than toPush.size() - ropePositions.size(): in extend mode ropePositions holds chain
    // ropes that are not in toPush, so the subtraction would underflow.
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

            // Both modes shift the whole rope column by one slot, symmetrically.
            // Retract: firstSlot is marked consumed so it gets no MOVING_PISTON entry, and the rope
            // below slides up to overwrite it.
            // Extend: every rope animates one slot in the push direction, leaving firstSlot empty
            // for serverFinaliseExtend to fill; the anchor slides further down via addBlockLine.
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
                    // No anchor (bare hanging rope) or the chain ran into another pulley body: the
                    // column still shortens by one if we walked any rope segments.
                    if (toPush.size() > contributionMark) {
                        contributedPulleys.add(pulley.pulleyPos());
                    } else if (extending && anchorIsAir) {
                        // Pure open air below the pulley (firstSlot == walkPos): nothing to push and
                        // no column to grow, but lowering must still drop a rope into firstSlot.
                        directRopePlacements.put(firstSlot, pulley.ropeBlock().defaultBlockState());
                        contributedPulleys.add(pulley.pulleyPos());
                    }
                    continue;
                }
                // Immovable anchor (obsidian, DESTROY reaction...). Fail the whole resolve so nothing
                // happens, rather than winding rope while leaving the anchor stuck.
                return false;
            }

            if (toPush.contains(walkPos)) {
                // Anchor already added by another pulley's sticky chain. Still a contribution,
                // since our column shifts along with the shared structure.
                contributedPulleys.add(pulley.pulleyPos());
                continue;
            }
            if (!addBlockLine(walkPos, pushDirection)) return false;
            if (toPush.size() > contributionMark) {
                contributedPulleys.add(pulley.pulleyPos());
            }
        }
        // Sticky branching pass, skipping ropes (no slime-via-rope chains).
        // Must be an indexed loop, like vanilla: branching mutates toPush while we walk it, and
        // newly added blocks have to be visited too. An iterator would throw here.
        for (int i = 0; i < toPush.size(); i++) {
            BlockPos pos = toPush.get(i);
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
                    // Stop at chain ropes. Otherwise a sticky anchor would count the rope above it
                    // as attached via the "either side sticky" rule and drag it along.
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

            // Chain reached the pulley's mouth: terminate successfully. The consumed rope gets
            // overwritten by whichever block in toPush has it as its destination.
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

    // Extend only: firstSlot -> rope state for chains that contributed. The mover flags the topmost
    // MOVING_PULLEY of each as carrying a trailing phantom, a rope animating out of the pulley into
    // firstSlot. Empty when retracting.
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

    // Extend only: firstSlot -> rope state for pulleys lowering into open air, where the rope is
    // placed directly with no animation. Empty when retracting.
    public Map<BlockPos, BlockState> getDirectRopePlacements() {
        return this.directRopePlacements;
    }

    // Pulleys follow the piston rule, so this defers to the piston pushability check: same limits,
    // push reactions and platform hooks, plus the movement blacklist. Our mixin on that method also
    // neutralises vanilla's !hasBlockEntity() rejection, which is what lets a pulley pull a chest.
    // On top of it, rope movement refuses ROPE_PUSH_BLACKLIST: a pulley shifts single blocks, so
    // anything whose partner wouldn't come along (doors, double blocks) would break.
    private static boolean isPullable(BlockState state, Level level, BlockPos pos,
                                      Direction movementDirection, boolean allowDestroy,
                                      Direction pulleyFacing) {
        if (state.is(ModTags.ROPE_PUSH_BLACKLIST)) return false;
        return PistonMovementHelper.isPushableByOurMovers(state, level, pos, movementDirection,
                allowDestroy, pulleyFacing);
    }
}
