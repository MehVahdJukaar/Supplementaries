package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//TODO: merge in 1.21
@Pseudo
@Mixin(FluidRenderer.class)
public abstract class CompatSodiumFluidRendererMixin {

    @Shadow
    @Final
    private QuadLightData quadLightData;

    @WrapOperation(method = "fluidHeight",
            remap = false,
            at = @At(value = "INVOKE",
                    remap = true,
                    target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z"))
    public boolean supplementaries$modifyLumiseneHeight(Fluid instance, Fluid above, Operation<Boolean> original) {
        return original.call(instance, above) || above.isSame(ModFluids.LUMISENE_FLUID.get());
    }

    @WrapOperation(method = "updateQuad(Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/light/LightPipeline;Lnet/minecraft/core/Direction;FLme/jellysquid/mods/sodium/client/model/color/ColorProvider;Lnet/minecraft/world/level/material/FluidState;)V",
            require = 0, //WHYYYY TODO: FIX MEEE
            at = @At(value = "INVOKE",
                    remap = false,
                    args = "log=true",
                    target = "Lme/jellysquid/mods/sodium/client/model/color/ColorProvider;getColors(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/core/BlockPos;Ljava/lang/Object;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;[I)V"),
            remap = false)
    public void supplementaries$modifyLumiseneEmissivity(ColorProvider<FluidState> instance,
                                                         WorldSlice worldSlice, BlockPos pos, Object o,
                                                         ModelQuadView modelQuadView, int[] ints, Operation<Void> original) {
        FluidState fluidState = (FluidState) o;
        original.call(instance, worldSlice, pos, o, modelQuadView, ints);

        if (fluidState.is(ModFluids.LUMISENE_FLUID.get())) {
            QuadLightData light = this.quadLightData;

            int minLight = ModFluids.LUMISENE_FAKE_LIGHT_EMISSION - 3;
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

    @ModifyArg(method = "render",
            remap = false,
            at = @At(value = "INVOKE",
                    remap = false,
                    target = "Lme/jellysquid/mods/sodium/client/model/light/LightPipelineProvider;getLighter(Lme/jellysquid/mods/sodium/client/model/light/LightMode;)Lme/jellysquid/mods/sodium/client/model/light/LightPipeline;"))
    public LightMode supplementaries$modifyLumiseneLight(LightMode lightMode, @Local Fluid fluid) {
        if (fluid == ModFluids.LUMISENE_FLUID.get()) {
            return Minecraft.getInstance().options.ambientOcclusion().get() ? LightMode.SMOOTH : LightMode.FLAT;
        }
        return lightMode;
    }

}
