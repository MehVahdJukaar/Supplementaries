package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Lantern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Lantern.class})
public abstract class LanternBlockMixin {

    @Inject(method = {"canSurvive"}, at = {@At("HEAD")}, cancellable = true)
    private void isValidPosition(BlockState state, LevelReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(state.getValue(Lantern.HANGING) && RopeBlock.isSupportingCeiling(pos.above(),worldIn))
            callbackInfoReturnable.setReturnValue(true);
    }

}

