package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.misc.CoopResolverHelper;
import net.mehvahdjukaar.supplementaries.common.misc.CoopResolverState;
import net.mehvahdjukaar.supplementaries.common.utils.ICooperativePiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Set;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin implements ICooperativePiston {

    @Unique
    private final CoopResolverState supp$coopState = new CoopResolverState();

    @Shadow @Final private List<BlockPos> toPush;
    @Shadow @Final private BlockPos pistonPos;

    @Override
    public void supp$setCooperators(Set<BlockPos> cooperators, int pushLimit,
                                    Direction pistonDirection, boolean extending) {
        this.supp$coopState.set(cooperators, pushLimit, pistonDirection, extending);
    }

    @Override
    public CoopResolverState supp$getCoopState() {
        return this.supp$coopState;
    }

    // Gate the resolve result on real cooperation: keep only cooperators whose start block
    // landed in toPush, then enforce the cooperative budget of 12 per contributing piston.
    @ModifyReturnValue(method = "resolve", at = @At("RETURN"))
    private boolean supp$gateOnRealCooperation(boolean original) {
        return CoopResolverHelper.gateResolve(original, pistonPos, toPush, supp$coopState);
    }

    // Extend each "is this the piston's own body?" check in addBlockLine to also recognise
    // cooperator piston positions as boundaries. Catches all three BlockPos.equals(pistonPos)
    // sites (origin, trailing-scan, forward-scan).
    @WrapOperation(method = "addBlockLine",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"))
    private boolean supp$wrapPistonEqualsCheck(BlockPos candidate, Object pistonPosArg,
                                               Operation<Boolean> original) {
        return CoopResolverHelper.wrapEqualsCheck(
                original.call(candidate, pistonPosArg), candidate, supp$coopState);
    }

    // Replace the two trailing-scan "> 12" limits with the cooperative limit.
    @Expression("? > @(12)")
    @ModifyExpressionValue(method = "addBlockLine", at = @At("MIXINEXTRAS:EXPRESSION"), require = 2)
    private int supp$modifyTrailingLimit(int original) {
        return supp$coopState.pushLimit;
    }

    // Replace the forward-scan ">= 12" limit with the cooperative limit.
    @Expression("? >= @(12)")
    @ModifyExpressionValue(method = "addBlockLine", at = @At("MIXINEXTRAS:EXPRESSION"), require = 1)
    private int supp$modifyForwardLimit(int original) {
        return supp$coopState.pushLimit;
    }
}
