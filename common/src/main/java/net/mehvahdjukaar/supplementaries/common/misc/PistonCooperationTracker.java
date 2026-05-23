package net.mehvahdjukaar.supplementaries.common.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;

import java.util.*;

/**
 * Tracks which pistons are trying to extend or retract on any given tick so that adjacent
 * pistons facing the same direction can cooperate to push/pull structures larger than 12 blocks.
 * <p>
 * Lifecycle:
 *  1. During tick T, checkIfExtend() registers each piston via markAttempting(). Pistons that
 *     successfully resolve cooperatively also mark each other as posted (extension only).
 *  2. Block events fire during tick T+1 via runBlockEvents. moveBlocks() reads cooperators
 *     from the tracker to set up the actual-movement resolver. The tracker is NOT cleared
 *     here — it retains tick T's data until a new markAttempting in a later tick resets it.
 * <p>
 * Entries are tagged with {@code extending} (true = extension, false = retraction) so that
 * stale extension data from an earlier tick does not leak into a later retraction's
 * moveBlocks (which would cause bizarre block movements like pulling random unstuck blocks).
 * <p>
 * Not thread-safe — intended for use on the server game loop only.
 */
public final class PistonCooperationTracker {

    private record AttemptInfo(Direction direction, boolean extending) {
    }

    private static long activeTick = Long.MIN_VALUE;
    private static final Map<Long, AttemptInfo> attemptingPistons = new HashMap<>();
    private static final Set<Long> postedPistons = new HashSet<>();

    private PistonCooperationTracker() {
    }

    private static void resetIfNewTick(long tick) {
        if (tick != activeTick) {
            activeTick = tick;
            attemptingPistons.clear();
            postedPistons.clear();
        }
    }

    /**
     * Called from checkIfExtend() as soon as a piston decides it wants to extend or retract.
     * Registers the piston so that later pistons on the same tick can discover it, and so
     * that moveBlocks (in the following tick) can find the cooperative group.
     */
    public static void markAttempting(BlockPos pos, Direction dir, boolean extending, long gameTick) {
        resetIfNewTick(gameTick);
        attemptingPistons.put(pos.asLong(), new AttemptInfo(dir, extending));
    }

    public static boolean hasPosted(BlockPos pos, long gameTick) {
        resetIfNewTick(gameTick);
        return postedPistons.contains(pos.asLong());
    }

    public static void markPosted(BlockPos pos, long gameTick) {
        resetIfNewTick(gameTick);
        postedPistons.add(pos.asLong());
    }

    /**
     * Returns all piston positions registered with the same direction AND same extending
     * state whose push chains could overlap with {@code pistonPos}'s chain.
     * <p>
     * Two geometric conditions must hold for a candidate to qualify:
     *  1. Its offset along the push axis is < MAX_PUSH_DEPTH — their push ranges overlap.
     *  2. Its offset in the perpendicular plane is > 0 and ≤ MAX_PUSH_DEPTH — it is offset
     *     sideways (not the same push column) and within a reachable distance.
     * <p>
     * Does NOT take a game tick — the tracker is reset only via markAttempting when a new
     * tick brings fresh registrations. This is required because block events fire in the
     * tick AFTER checkIfExtend, so moveBlocks needs to read tracker data from the previous
     * tick. Stale data from much earlier ticks is guarded against by the {@code extending}
     * filter (e.g. retraction data won't match an extension's getCooperators call).
     */
    public static Set<BlockPos> getCooperators(BlockPos pistonPos, Direction dir, boolean extending) {
        if (attemptingPistons.size() <= 1) return Collections.emptySet();

        Set<BlockPos> cooperators = new HashSet<>();
        Direction.Axis pushAxis = dir.getAxis();

        for (Map.Entry<Long, AttemptInfo> entry : attemptingPistons.entrySet()) {
            AttemptInfo info = entry.getValue();
            if (!dir.equals(info.direction)) continue;
            if (extending != info.extending) continue;

            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(pistonPos)) continue;

            int dx = candidate.getX() - pistonPos.getX();
            int dy = candidate.getY() - pistonPos.getY();
            int dz = candidate.getZ() - pistonPos.getZ();

            int pushOffset = Math.abs(pushAxis == Direction.Axis.X ? dx :
                    pushAxis == Direction.Axis.Y ? dy : dz);
            if (pushOffset >= PistonStructureResolver.MAX_PUSH_DEPTH) continue;

            int perpX = pushAxis == Direction.Axis.X ? 0 : dx;
            int perpY = pushAxis == Direction.Axis.Y ? 0 : dy;
            int perpZ = pushAxis == Direction.Axis.Z ? 0 : dz;
            if (perpX == 0 && perpY == 0 && perpZ == 0) continue; // collinear, same column
            int perpDist = Math.max(Math.abs(perpX), Math.max(Math.abs(perpY), Math.abs(perpZ)));
            if (perpDist > PistonStructureResolver.MAX_PUSH_DEPTH) continue;

            cooperators.add(candidate);
        }
        return cooperators;
    }
}
