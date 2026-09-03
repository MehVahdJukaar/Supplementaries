package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PistonCooperationState;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICooperativePiston;
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
    private final PistonCooperationState supp$cooperationState = new PistonCooperationState();

    @Shadow @Final private List<BlockPos> toPush;
    @Shadow @Final private BlockPos pistonPos;

    @Override
    public void supp$setCooperators(Set<BlockPos> cooperators, Direction pistonDirection, boolean extending) {
        this.supp$cooperationState.set(cooperators, pistonDirection, extending);
    }

    @Override
    public PistonCooperationState supp$getCooperationState() {
        return this.supp$cooperationState;
    }

    @ModifyReturnValue(method = "resolve", at = @At("RETURN"))
    private boolean supp$gateOnRealCooperation(boolean original) {
        return supp$cooperationState.gateResolve(original, pistonPos, toPush);
    }

    // Cooperator piston bodies act as walls too; catches all three BlockPos.equals(pistonPos) sites.
    @WrapOperation(method = "addBlockLine",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"))
    private boolean supp$wrapPistonEqualsCheck(BlockPos candidate, Object pistonPosArg,
                                               Operation<Boolean> original) {
        return supp$cooperationState.wrapEqualsCheck(
                original.call(candidate, pistonPosArg), candidate);
    }

    @Expression("? > @(12)")
    @ModifyExpressionValue(method = "addBlockLine", at = @At("MIXINEXTRAS:EXPRESSION"), require = 2)
    private int supp$modifyTrailingLimit(int original) {
        return supp$cooperationState.getPushLimit();
    }

    @Expression("? >= @(12)")
    @ModifyExpressionValue(method = "addBlockLine", at = @At("MIXINEXTRAS:EXPRESSION"), require = 1)
    private int supp$modifyForwardLimit(int original) {
        return supp$cooperationState.getPushLimit();
    }
}
