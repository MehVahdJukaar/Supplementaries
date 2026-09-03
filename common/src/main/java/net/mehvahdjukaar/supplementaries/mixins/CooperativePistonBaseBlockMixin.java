package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PistonCooperationLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBaseBlock.class)
public class CooperativePistonBaseBlockMixin {

    @WrapOperation(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"))
    private boolean supp$tryExtendTogether(PistonStructureResolver resolver, Operation<Boolean> original,
                                           @Local(argsOnly = true) Level level,
                                           @Local(argsOnly = true) BlockPos pos,
                                           @Local Direction direction) {
        return PistonCooperationLogic.tryExtendTogether(resolver, () -> original.call(resolver), level, pos, direction);
    }

    //the retract branch has no resolver, just a block event
    @Inject(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V",
                    ordinal = 1))
    private void supp$markRetractingThisTick(Level level, BlockPos pos, BlockState state, CallbackInfo ci,
                                             @Local Direction direction) {
        PistonCooperationLogic.markRetractingThisTick(level, pos, direction);
    }

    @WrapOperation(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"))
    private boolean supp$resolveMoveTogether(PistonStructureResolver resolver, Operation<Boolean> original,
                                             @Local(argsOnly = true) Level level,
                                             @Local(argsOnly = true) BlockPos pos,
                                             @Local(argsOnly = true) Direction facing,
                                             @Local(argsOnly = true) boolean extending) {
        return PistonCooperationLogic.resolveMoveTogether(resolver, () -> original.call(resolver), level, pos, facing, extending);
    }
}
