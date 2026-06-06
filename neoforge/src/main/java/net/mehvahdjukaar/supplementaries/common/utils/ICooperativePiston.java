package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.supplementaries.common.misc.CoopResolverState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public interface ICooperativePiston {
    /**
     * Configure cooperators on this resolver. Subsequent {@code resolve()} runs use the
     * boosted push limit and treat cooperator positions as boundaries inside
     * {@code addBlockLine}, and the post-resolve gate filters out free-riders.
     */
    void supp$setCooperators(Set<BlockPos> cooperatingPistons, int pushLimit,
                             Direction pistonDirection, boolean extending);

    /** Direct access to the underlying state — used by mixins on resolver subclasses (Zeta). */
    CoopResolverState supp$getCoopState();
}