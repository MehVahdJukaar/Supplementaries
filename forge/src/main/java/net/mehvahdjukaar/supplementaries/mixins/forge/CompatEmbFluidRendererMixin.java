package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.forge.ModFluidsImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(FluidRenderer.class)
public abstract class CompatEmbFluidRendererMixin {

    @Shadow
    @Final
    private QuadLightData quadLightData;

    @WrapOperation(method = "fluidHeight", remap = false,
            require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z"))
    public boolean supplementaries$modifyLumiseneHeight(Fluid instance, Fluid above, Operation<Boolean> original) {
        return original.call(instance, above) || above.isSame(ModFluids.LUMISENE_FLUID.get());
    }

    @Inject(method = "updateQuad",
            require = 0,
            at = @At(value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lme/jellysquid/mods/sodium/client/model/color/ColorProvider;getColors(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/core/BlockPos;Ljava/lang/Object;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;[I)V"),
            remap = false)
    public void supplementaries$modifyLumiseneEmissivity(ModelQuadView quad, WorldSlice world, BlockPos pos, LightPipeline lighter, Direction dir, float brightness, ColorProvider<FluidState> colorProvider, FluidState fluidState, CallbackInfo ci) {

        if (fluidState.is(ModFluids.LUMISENE_FLUID.get())) {
            QuadLightData light = this.quadLightData;

            int minLight = ModFluidsImpl.LUMISENE_FAKE_LIGHT_EMISSION - 3;
            for (int j = 0; j < light.lm.length; j++) {
                int l = light.lm[j];
                int bl = LightTexture.block(l);
                int sl = LightTexture.sky(l);
                if (bl < minLight) {
                    bl = minLight;
                }
                // this removes smooth lighting from lights lower than me
                light.lm[j] = LightTexture.pack(bl, sl);

                // no shading on emissive stuff!
                //TODO: this cant be correct! without however stuff is shader when against blocks
                light.br[j] = 1.0F;
            }
        }
    }



    /*
    @Inject(method = "calculateAverageHeight",
            at = @At("HEAD"), cancellable = true)
    public void supplementaries$modifyLumiseneHeight(BlockAndTintGetter level, Fluid fluid, float g, float h, float i, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        ModFluidsImpl.messWithAvH(level, fluid, g, h, i, pos, cir);
    }

    @ModifyExpressionValue(method = "tesselate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    public int supplementaries$modifyLumiseneLight(int original, @Local  Fluid fluid) {
       return ModFluidsImpl.messWithFluidLight(original, fluid);
    }*/

    @ModifyArg(method = "render", remap = false,
            require = 0,
            at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/model/light/LightPipelineProvider;getLighter(Lme/jellysquid/mods/sodium/client/model/light/LightMode;)Lme/jellysquid/mods/sodium/client/model/light/LightPipeline;"))
    public LightMode supplementaries$modifyLumiseneLight(LightMode lightMode, @Local Fluid fluid) {
        if (fluid == ModFluids.LUMISENE_FLUID.get()) {
            return Minecraft.getInstance().options.ambientOcclusion().get() ? LightMode.SMOOTH : LightMode.FLAT;
        }
        return lightMode;
    }

}
