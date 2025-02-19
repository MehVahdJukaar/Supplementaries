package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {

    @Inject(method = "canSurvive", at = @At(value = "INVOKE",
            ordinal = 0,
            shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"), cancellable = true)
    public void supp$surviveOnPlanter(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir,
                                      @Local(ordinal = 1) BlockState belowState){
        if(belowState.getBlock() instanceof PlanterBlock){
            cir.setReturnValue(true);
        }
    }
}
