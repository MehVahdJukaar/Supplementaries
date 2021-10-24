package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LanternBlock.class})
public abstract class LanternBlockPlacementMixin {

    @Inject(method = {"canSurvive"}, at = {@At("HEAD")}, cancellable = true)
    private void isValidPosition(BlockState state, LevelReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (state.getValue(LanternBlock.HANGING) && RopeBlock.isSupportingCeiling(pos.above(), worldIn))
            callbackInfoReturnable.setReturnValue(true);
    }

}

