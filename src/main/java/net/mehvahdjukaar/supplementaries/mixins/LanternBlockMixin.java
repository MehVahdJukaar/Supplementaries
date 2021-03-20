package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LanternBlock.class})
public class LanternBlockMixin {

    @Inject(method = {"isValidPosition"}, at = {@At("HEAD")}, cancellable = true)
    private void isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(state.get(LanternBlock.HANGING) && RopeBlock.isSupportingCeiling(pos.up(),worldIn))
            callbackInfoReturnable.setReturnValue(true);
    }
}

