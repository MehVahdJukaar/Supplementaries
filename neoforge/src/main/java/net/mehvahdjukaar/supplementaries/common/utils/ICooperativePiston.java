package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface ICooperativePiston {
    void supp$setCooperators(Set<BlockPos> cooperatingPistons, int pushLimit);

    /**
     * Cooperators whose start block ended up in the primary's toPush after resolve —
     * i.e. those actually sharing the pushed structure. Empty until resolve() has run.
     */
    Set<BlockPos> supp$getContributingCooperators();
}