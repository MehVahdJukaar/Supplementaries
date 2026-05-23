package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import java.util.List;
import java.util.Set;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin implements ICooperativePiston {

    @Unique
    private Set<BlockPos> supp$cooperatingPistons = Collections.emptySet();
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
    private boolean addBlockLine(BlockPos pos, Direction dir) {
        throw new AssertionError();
    }

    @Override
    public void supp$setCooperators(Set<BlockPos> cooperatingPistons, int pushLimit) {
        this.supp$cooperatingPistons = cooperatingPistons;
        this.supp$pushLimit = pushLimit;
    }

    /**
     * After the primary piston's chain is built, extend the resolver to also cover each
     * cooperating piston's chain. Blocks already in toPush (shared structure) are skipped.
     * Non-pushable cooperating starts are skipped (return ignored) so the primary's push
     * still succeeds — e.g. MOVING_PISTON when a cooperative move already happened.
     * <p>
     * For extension, a cooperator's startPos is {@code cooperator + pistonDirection*1}
     * (the first block in front of the piston). For retraction, it's
     * {@code cooperator + pistonDirection*2} (the block being pulled back, two ahead of
     * the body).
     */
    @WrapOperation(method = "resolve",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;addBlockLine(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean supp$wrapResolveAddBlockLine(PistonStructureResolver self, BlockPos startPos, Direction dir,
                                                 Operation<Boolean> original) {
        if (!original.call(self, startPos, dir)) return false;
        int cooperatorStartOffset = extending ? 1 : 2;
        for (BlockPos cooperatorPos : supp$cooperatingPistons) {
            BlockPos cooperatorStart = cooperatorPos.relative(pistonDirection, cooperatorStartOffset);
            if (toPush.contains(cooperatorStart)) continue;
            addBlockLine(cooperatorStart, dir);
        }
        return true;
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
