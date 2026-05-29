package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.utils.ICooperativePiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin implements ICooperativePiston {

    @Unique
    private Set<BlockPos> supp$cooperatingPistons = Collections.emptySet();
    @Unique
    private Set<BlockPos> supp$contributingCooperators = Collections.emptySet();
    @Unique
    private int supp$pushLimit = 12;

    @Shadow
    @Final
    private Direction pushDirection;
    @Shadow
    @Final
    private Direction pistonDirection;
    @Shadow
    @Final
    private boolean extending;
    @Shadow
    @Final
    private List<BlockPos> toPush;
    @Shadow
    @Final
    private BlockPos pistonPos;

    @Shadow
    private boolean addBlockLine(BlockPos pos, Direction dir) {
        throw new AssertionError();
    }

    @Override
    public void supp$setCooperators(Set<BlockPos> cooperatingPistons, int pushLimit) {
        this.supp$cooperatingPistons = cooperatingPistons;
        this.supp$pushLimit = pushLimit;
        this.supp$contributingCooperators = Collections.emptySet();
        Supplementaries.LOGGER.info("[COOP] setCooperators: piston={} dir={} extending={} cooperators={} limit={}",
                pistonPos, pistonDirection, extending, cooperatingPistons, pushLimit);
    }

    @Override
    public Set<BlockPos> supp$getContributingCooperators() {
        return supp$contributingCooperators;
    }

    /**
     * Gate the resolve outcome on real cooperation. Vanilla {@code resolve} first calls
     * {@code addBlockLine} for the primary's straight chain, then iterates {@code toPush}
     * and calls {@code addBranchingBlocks} on each sticky block — that second pass is
     * where slime/honey perpendicular branching happens and grows the connected structure.
     * <p>
     * Only after BOTH passes complete can we tell whether a cooperator is actually part of
     * the shared structure: its start block must have landed in {@code toPush} (either via
     * the primary's chain or via slime branching). Free-rider pistons — same direction, in
     * range, but pushing nothing connected — are filtered out here and don't get to donate
     * their 12-block budget.
     * <p>
     * Effective cap: {@code 12 × (1 + contributingCooperators)}. The boosted
     * {@code supp$pushLimit} stays in effect inside addBlockLine as the worst-case ceiling
     * during chain construction; this post-check enforces the real per-participant budget.
     * <p>
     * Start offsets: extension → {@code +pistonDirection*1} (first block in front of the
     * piston); retraction → {@code +pistonDirection*2} (the block being pulled back).
     */
    @ModifyReturnValue(method = "resolve", at = @At("RETURN"))
    private boolean supp$gateOnRealCooperation(boolean original) {
        if (!original) {
            Supplementaries.LOGGER.info("[COOP] resolve@{} → false (vanilla failed)", pistonPos);
            return false;
        }
        if (supp$cooperatingPistons.isEmpty()) {
            Supplementaries.LOGGER.info("[COOP] resolve@{} → true (no cooperators set)", pistonPos);
            return true;
        }

        int cooperatorStartOffset = extending ? 1 : 2;
        Set<BlockPos> contributing = new HashSet<>();
        for (BlockPos cooperatorPos : supp$cooperatingPistons) {
            if (cooperatorPos.equals(pistonPos)) continue; // skip primary — its start is trivially in toPush
            BlockPos cooperatorStart = cooperatorPos.relative(pistonDirection, cooperatorStartOffset);
            boolean inToPush = toPush.contains(cooperatorStart);
            Supplementaries.LOGGER.info("[COOP]   cooperator={} start={} inToPush={}",
                    cooperatorPos, cooperatorStart, inToPush);
            if (inToPush) contributing.add(cooperatorPos);
        }
        supp$contributingCooperators = contributing;

        int effectiveLimit = (1 + contributing.size()) * 12;
        boolean ok = toPush.size() <= effectiveLimit;
        Supplementaries.LOGGER.info("[COOP] resolve@{} toPush.size={} contributing={} effectiveLimit={} → {}",
                pistonPos, toPush.size(), contributing, effectiveLimit, ok);
        return ok;
    }

    /**
     * Replace the two trailing-scan BIPUSH 12 limits (IF_ICMPLE) with the cooperative limit.
     */
    @Expression("? > @(12)")
    @ModifyExpressionValue(method = "addBlockLine",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            require = 2)
    private int supp$modifyTrailingLimit(int original) {
        return supp$pushLimit;
    }

    /**
     * Replace the forward-scan BIPUSH 12 limit (IF_ICMPLT) with the cooperative limit.
     */
    @Expression("? >= @(12)")
    @ModifyExpressionValue(method = "addBlockLine",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            require = 1)
    private int supp$modifyForwardLimit(int original) {
        return supp$pushLimit;
    }

    /**
     * Extend each "is this the piston's own body?" check in addBlockLine to also recognise
     * any cooperating piston position as a boundary. Targets all three INVOKEVIRTUAL
     * BlockPos.equals calls in addBlockLine (origin check, trailing-scan skip, forward-scan
     * boundary), which are the only BlockPos.equals calls in that method.
     */
    @WrapOperation(method = "addBlockLine",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"))
    private boolean supp$wrapPistonEqualsCheck(BlockPos candidate, Object pistonPosArg,
                                               Operation<Boolean> original) {
        return original.call(candidate, pistonPosArg) || supp$cooperatingPistons.contains(candidate);
    }
}
