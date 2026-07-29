package net.mehvahdjukaar.supplementaries.common.misc;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared cooperative-piston logic, invoked from both the vanilla {@code PistonStructureResolver}
 * mixin and the optional mixin on Quark/Zeta's wrapper resolver. Keeping the logic here means
 * the two mixins only contribute the field shadows for their respective {@code toPush}
 * variants and forward to these helpers.
 */
public class CoopResolverHelper {
    private static final int PER_PISTON_LIMIT = 12;

    /**
     * Runs after the resolver's {@code resolve()} returns. Filters cooperators down to those
     * whose start block actually landed in {@code toPush} (free-riders excluded), stores the
     * result in {@code state.contributingCooperators}, and gates on the cooperative budget.
     * <p>
     * Effective cap: {@code PER_PISTON_LIMIT * (1 + contributingCooperators.size())}. The boosted
     * {@code state.pushLimit} stays in effect inside {@code addBlockLine} as the worst-case
     * ceiling during chain construction; this post-check enforces the real per-participant
     * budget.
     */
    public static boolean gateResolve(boolean originalResult, BlockPos pistonPos,
                                      List<BlockPos> toPush, CoopResolverState state) {
        if (!originalResult) return false;
        if (state.cooperatingPistons.isEmpty()) return true;
        if (state.pistonDirection == null) return true;

        int offset = state.extending ? 1 : 2;
        Set<BlockPos> contributing = new HashSet<>();
        for (BlockPos cooperatorPos : state.cooperatingPistons) {
            if (cooperatorPos.equals(pistonPos)) continue;
            BlockPos start = cooperatorPos.relative(state.pistonDirection, offset);
            if (toPush.contains(start)) contributing.add(cooperatorPos);
        }
        state.contributingCooperators = contributing;
        return toPush.size() <= (1 + contributing.size()) * PER_PISTON_LIMIT;
    }

    /**
     * Extends each "is this the piston's own body?" boundary check in {@code addBlockLine}
     * to also recognise cooperator piston positions: those columns are independent and
     * shouldn't be traversed into.
     */
    public static boolean wrapEqualsCheck(boolean originalResult, BlockPos candidate,
                                          CoopResolverState state) {
        return originalResult || state.cooperatingPistons.contains(candidate);
    }
}
