package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

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

    private Set<BlockPos> cooperatingPistons = Collections.emptySet();
    private Set<BlockPos> contributingCooperators = Collections.emptySet();
    @Nullable
    private Direction pistonDirection;
    private boolean extending;


    public void set(Set<BlockPos> cooperators, Direction pistonDirection, boolean extending) {
        this.cooperatingPistons = cooperators;
        this.pistonDirection = pistonDirection;
        this.extending = extending;
        this.contributingCooperators = Collections.emptySet();
    }

    /**
     * Worst-case ceiling for this resolve: every registered participant chipping in its own budget.
     * Deriving it instead of storing it keeps it in lockstep with the post-resolve gate, which caps
     * on the participants that actually contributed.
     */
    public int getPushLimit() {
        return Math.max(1, cooperatingPistons.size()) * PistonMovementHelper.perPistonPushLimit();
    }

    public Iterable<BlockPos> getContributingCooperators() {
        return contributingCooperators;
    }

    /**
     * Take over the contributing set another resolver's state computed. Needed when Zeta wraps the
     * vanilla resolver but delegates {@code resolve()} to it: the gate then runs on the delegate's
     * state while callers still read the wrapper's.
     */
    public void adoptContributingFrom(PistonCoopResolverState other) {
        this.contributingCooperators = other.contributingCooperators;
    }

    /**
     * Runs after the resolver's {@code resolve()} returns. Filters cooperators down to those whose
     * start block actually landed in {@code toPush} (free-riders excluded), remembers them as the
     * contributing set, and gates on the cooperative budget.
     * <p>
     * Effective cap: {@link perPistonPushLimit()} times one plus the contributing cooperators. The
     * boosted {@link #getPushLimit()} stays in effect inside {@code addBlockLine} as the worst-case
     * ceiling during chain construction; this post-check enforces the real per-participant budget.
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
        return toPush.size() <= (1 + contributing.size()) * PistonMovementHelper.perPistonPushLimit();
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
