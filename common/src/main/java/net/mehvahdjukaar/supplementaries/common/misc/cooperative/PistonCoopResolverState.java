package net.mehvahdjukaar.supplementaries.common.misc.cooperative;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Cooperative-piston state attached to a {@code PistonStructureResolver} (or any subclass like
// Quark/Zeta's wrapper). Holds the cooperators registered for this resolve, and once the gate
// check has run, the subset that actually contributed to the pushed structure. The two hooks the
// resolver mixins call live here too, so those mixins only need to shadow their respective
// {@code toPush} variant and forward to this class.
public class PistonCoopResolverState {

    private static final int PER_PISTON_LIMIT = 12;

    private Set<BlockPos> cooperatingPistons = Collections.emptySet();
    private Set<BlockPos> contributingCooperators = Collections.emptySet();
    private int pushLimit = 12;
    @Nullable
    private Direction pistonDirection;
    private boolean extending;

    public void set(Set<BlockPos> cooperators, int pushLimit, Direction pistonDirection, boolean extending) {
        this.cooperatingPistons = cooperators;
        this.pushLimit = pushLimit;
        this.pistonDirection = pistonDirection;
        this.extending = extending;
        this.contributingCooperators = Collections.emptySet();
    }

    public int getPushLimit() {
        return pushLimit;
    }

    public Iterable<BlockPos> getContributingCooperators() {
        return contributingCooperators;
    }

    /**
     * Runs after the resolver's {@code resolve()} returns. Filters cooperators down to those whose
     * start block actually landed in {@code toPush} (free-riders excluded), remembers them as the
     * contributing set, and gates on the cooperative budget.
     * <p>
     * Effective cap: {@code PER_PISTON_LIMIT * (1 + contributing)}. The boosted {@link #pushLimit}
     * stays in effect inside {@code addBlockLine} as the worst-case ceiling during chain
     * construction; this post-check enforces the real per-participant budget.
     */
    public boolean gateResolve(boolean originalResult, BlockPos pistonPos, List<BlockPos> toPush) {
        if (!originalResult) return false;
        if (this.cooperatingPistons.isEmpty()) return true;
        if (this.pistonDirection == null) return true;

        int offset = this.extending ? 1 : 2;
        Set<BlockPos> contributing = new HashSet<>();
        for (BlockPos cooperatorPos : this.cooperatingPistons) {
            if (cooperatorPos.equals(pistonPos)) continue;
            BlockPos start = cooperatorPos.relative(this.pistonDirection, offset);
            if (toPush.contains(start)) contributing.add(cooperatorPos);
        }
        this.contributingCooperators = contributing;
        return toPush.size() <= (1 + contributing.size()) * PER_PISTON_LIMIT;
    }

    /**
     * Extends each "is this the piston's own body?" boundary check in {@code addBlockLine}
     * to also recognise cooperator piston positions: those columns are independent and
     * shouldn't be traversed into.
     */
    public boolean wrapEqualsCheck(boolean originalResult, BlockPos candidate) {
        return originalResult || this.cooperatingPistons.contains(candidate);
    }
}
