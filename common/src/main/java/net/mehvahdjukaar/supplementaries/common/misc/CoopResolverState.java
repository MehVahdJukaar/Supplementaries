package net.mehvahdjukaar.supplementaries.common.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Cooperative-piston state attached to a {@code PistonStructureResolver} (or any subclass
 * like Quark/Zeta's wrapper). Captures the cooperators registered for this resolve and
 * — after the gate check runs — the subset that actually contributed to the pushed structure.
 */
public class CoopResolverState {
    public Set<BlockPos> cooperatingPistons = Collections.emptySet();
    public Set<BlockPos> contributingCooperators = Collections.emptySet();
    public int pushLimit = 12;
    @Nullable public Direction pistonDirection;
    public boolean extending;

    public void set(Set<BlockPos> cooperators, int pushLimit, Direction pistonDirection, boolean extending) {
        this.cooperatingPistons = cooperators;
        this.pushLimit = pushLimit;
        this.pistonDirection = pistonDirection;
        this.extending = extending;
        this.contributingCooperators = Collections.emptySet();
    }
}
