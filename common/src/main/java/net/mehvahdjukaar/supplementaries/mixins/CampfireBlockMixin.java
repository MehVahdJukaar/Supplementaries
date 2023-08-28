package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {

    @Inject(method = "isSmokeSource", at = @At("HEAD"), cancellable = true)
    public void isSmokeSource(BlockState state, CallbackInfoReturnable<Boolean> info ) {
        if(state.is(ModRegistry.FLAX_BLOCK.get()))
            info.setReturnValue(true);
    }
}
