package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfireMixin {

    @Inject(method = "isSmokeSource", at = @At("HEAD"), cancellable = true)
    public void isSmokeSource(BlockState state, CallbackInfoReturnable<Boolean> info ) {
        if(state.getBlock() == ModRegistry.FLAX_BLOCK.get())
            info.setReturnValue(true);
    }
}
