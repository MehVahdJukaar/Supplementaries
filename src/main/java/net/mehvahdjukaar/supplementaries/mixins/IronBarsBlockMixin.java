package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({IronBarsBlock.class})
public abstract class IronBarsBlockMixin {


    @Inject(method = "attachsTo", at = @At("TAIL"), cancellable = true)
    private void isAcceptableNeighbour(BlockState state, boolean b, CallbackInfoReturnable<Boolean> cir) {
        boolean r = cir.getReturnValue();
        if(!r && state.is(ModTags.PANE_CONNECTION)){
            //TODO: fix connection
            //state.getBlock() instanceof IronGateBlock && FenceGateBlock.connectsToDirection(state,di)
            cir.setReturnValue(true);
        }

    }
}
