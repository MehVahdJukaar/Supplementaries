package net.mehvahdjukaar.supplementaries.common.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;

import java.util.*;

/**
 * Tracks which pistons are trying to extend on any given tick so that adjacent pistons
 * facing the same direction can cooperate to push structures larger than 12 blocks.
 * <p>
 * Lifecycle per tick:
 * 1. checkIfExtend() calls markAttempting() for every piston that decides to extend.
 * 2. The LAST piston in a cooperating group (chronologically) finds the others via
 * getCooperators() and posts block events for all of them, marking each via markPosted().
 * 3. moveBlocks() calls getCooperators() to give the actual-movement resolver the same
 * combined push limit.
 * <p>
 * State is keyed by gameTick and cleared automatically when the tick advances, so no
 * manual cleanup is needed.
 * <p>
 * Not thread-safe — intended for use on the server game loop only.
 */
public final class PistonCooperationTracker {

    private static long activeTick = -1;
    /**
     * pistonPos.asLong() → pushDirection for every piston that called markAttempting this tick.
     */
    private static final Map<Long, Direction> attemptingPistons = new HashMap<>();
    /**
     * pistonPos.asLong() for every piston whose block event has already been posted this tick.
     */
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
     * Called from checkIfExtend() as soon as a piston decides it wants to extend.
     * Registers the piston so that later pistons on the same tick can discover it.
     */
    public static void markAttempting(BlockPos pos, Direction dir, long gameTick) {

        resetIfNewTick(gameTick);
        attemptingPistons.put(pos.asLong(), dir);
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
     * Returns all piston positions that registered as attempting to extend this tick in the
     * same push direction whose push chains could overlap with {@code pistonPos}'s chain.
     * <p>
     * Two conditions must hold for a candidate to qualify:
     * 1. Its offset along the push axis is < MAX_PUSH_DEPTH — their push ranges overlap.
     * 2. Its offset in the perpendicular plane is > 0 and ≤ MAX_PUSH_DEPTH — it is offset
     * sideways (not the same push column) and within a reachable distance.
     * <p>
     * This intentionally does NOT do a line-scan from the piston position, which was the
     * previous approach and failed when cooperating pistons are at different heights along
     * the push axis (e.g. a slime wall where one piston is 1 block lower than the other).
     * <p>
     * Works correctly in both orderings: whichever piston runs last finds all earlier ones.
     * <p>
     * Must take the current game tick so that stale registrations from earlier ticks
     * (e.g. an extension tick) are discarded when called later (e.g. during a retraction
     * tick's moveBlocks, where no markAttempting fires because retraction's checkIfExtend
     * never creates a resolver).
     */
    public static Set<BlockPos> getCooperators(BlockPos pistonPos, Direction dir, long gameTick) {
        System.out.println("getCooperators called at tick " + gameTick + ", activeTick=" + activeTick + ", attempting=" + attemptingPistons.keySet());
        resetIfNewTick(gameTick);
        if (attemptingPistons.size() <= 1) return Collections.emptySet();

        Set<BlockPos> cooperators = new HashSet<>();
        Direction.Axis pushAxis = dir.getAxis();

        for (Map.Entry<Long, Direction> entry : attemptingPistons.entrySet()) {
            if (!dir.equals(entry.getValue())) continue;

            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(pistonPos)) continue;

            int dx = candidate.getX() - pistonPos.getX();
            int dy = candidate.getY() - pistonPos.getY();
            int dz = candidate.getZ() - pistonPos.getZ();

            // Push-axis offset: must be < MAX_PUSH_DEPTH so the two push ranges overlap.
            int pushOffset = Math.abs(pushAxis == Direction.Axis.X ? dx :
                    pushAxis == Direction.Axis.Y ? dy : dz);
            if (pushOffset >= PistonStructureResolver.MAX_PUSH_DEPTH) continue;

            // Perpendicular offset: must be non-zero (different push column) and ≤ MAX_PUSH_DEPTH.
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