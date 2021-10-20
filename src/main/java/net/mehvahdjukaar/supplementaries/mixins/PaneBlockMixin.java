package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PaneBlock.class})
public abstract class PaneBlockMixin {


    @Inject(method = "attachsTo", at = @At("TAIL"), cancellable = true)
    private void isAcceptableNeighbour(BlockState state, boolean p_220112_2_, CallbackInfoReturnable<Boolean> cir) {
        boolean r = cir.getReturnValue();
        if (!r && state.is(ModTags.PANE_CONNECTION)) {
            //TODO: fix connection
            //state.getBlock() instanceof IronGateBlock && FenceGateBlock.connectsToDirection(state,di)
            cir.setReturnValue(true);
        }

    }
}
