package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.forge.ModFluids;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {

    @Inject(method = "getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F",
            at = @At("HEAD"), cancellable = true)
    public void supplementaries$modifyLumiseneHeight(BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Float> cir){
        if(fluid == ModFluids.STILL_LUMISENE.get()){
            ModFluids.messWithFluidH(level, fluid, pos, blockState, fluidState, cir);
        }
    }

    @Inject(method = "calculateAverageHeight",
            at = @At("HEAD"), cancellable = true)
    public void supplementaries$modifyLumiseneHeight(BlockAndTintGetter level, Fluid fluid, float g, float h, float i, BlockPos pos, CallbackInfoReturnable<Float> cir){
        if(fluid == ModFluids.STILL_LUMISENE.get()){
            ModFluids.messWithAvH(level, fluid, g, h, i, pos, cir);
        }
    }
}
