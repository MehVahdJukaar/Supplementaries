package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Cooperative-piston state attached to each PistonStructureResolver (vanilla or Zeta's wrapper).
// The resolver mixins shadow their own toPush variant and forward here.
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

    // Worst-case ceiling while the chain builds; gateResolve enforces the real per-contributor
    // budget afterwards.
    public int getPushLimit() {
        return Math.max(1, cooperatingPistons.size()) * PistonMovementHelper.perPistonPushLimit();
    }

    public Iterable<BlockPos> getContributingCooperators() {
        return contributingCooperators;
    }

    // For Zeta delegating resolve() to its vanilla parent: the gate runs on the parent's state
    // while callers still read the wrapper's.
    public void adoptContributingFrom(PistonCoopResolverState other) {
        this.contributingCooperators = other.contributingCooperators;
    }

    // Post-resolve: keeps only cooperators whose start block actually landed in toPush, then gates
    // on the real budget, perPistonPushLimit per contributor plus this piston.
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

    // Cooperator piston bodies count as walls in addBlockLine.
    public boolean wrapEqualsCheck(boolean originalResult, BlockPos candidate) {
        return originalResult || this.cooperatingPistons.contains(candidate);
    }
}
