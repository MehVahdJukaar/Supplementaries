package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
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
    private int pushLimit = perPistonPushLimit();
    @Nullable
    private Direction pistonDirection;
    private boolean extending;

    /**
     * How many blocks a single piston may push, which is what each cooperator contributes to the
     * pooled budget. Vanilla's {@link PistonStructureResolver#MAX_PUSH_DEPTH} unless Zeta's piston
     * resolver is in use, in which case its configurable limit (Zeta general config,
     * {@code pistonPushLimit}) is authoritative: cooperation has to scale off the same number the
     * user configured, or raising it would silently stop cooperation from adding anything and
     * lowering it would let us push past what they asked for.
     */
    public static int perPistonPushLimit() {
        if (CompatHandler.QUARK) return QuarkCompat.getPistonPushLimit();
        return PistonStructureResolver.MAX_PUSH_DEPTH;
    }

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
     * Effective cap: {@link #perPistonPushLimit()} times one plus the contributing cooperators. The
     * boosted {@link #pushLimit} stays in effect inside {@code addBlockLine} as the worst-case
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
        return toPush.size() <= (1 + contributing.size()) * perPistonPushLimit();
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
