package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//attached to each PistonStructureResolver, vanilla or Zeta's
public class PistonCooperationState {

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

    public int getPushLimit() {
        return Math.max(1, cooperatingPistons.size()) * PistonMovementHelper.getPerPistonPushLimit();
    }

    public Iterable<BlockPos> getContributingCooperators() {
        return contributingCooperators;
    }

    public void adoptContributingFrom(PistonCooperationState other) {
        this.contributingCooperators = other.contributingCooperators;
    }

    //drops cooperators that didn't contribute, then checks the pooled budget
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
        return toPush.size() <= (1 + contributing.size()) * PistonMovementHelper.getPerPistonPushLimit();
    }

    public boolean wrapEqualsCheck(boolean originalResult, BlockPos candidate) {
        return originalResult || this.cooperatingPistons.contains(candidate);
    }
}
