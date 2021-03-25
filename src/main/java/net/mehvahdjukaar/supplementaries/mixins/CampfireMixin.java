package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfireMixin {

    @Inject(method = "isSmokeSource", at = @At("HEAD"), cancellable = true)
    public void isSmokeSource(BlockState state, CallbackInfoReturnable<Boolean> info ) {
        if(state.getBlock() == Registry.FLAX_BLOCK.get())
            info.setReturnValue(true);
    }
}
