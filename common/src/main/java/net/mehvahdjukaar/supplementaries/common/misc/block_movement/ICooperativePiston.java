package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public interface ICooperativePiston {
    // only needed for our zeta hacks, just delegates to coopstate.set
    void supp$setCooperators(Set<BlockPos> cooperatingPistons, Direction pistonDirection, boolean extending);

    PistonCooperationState supp$getCooperationState();
}
