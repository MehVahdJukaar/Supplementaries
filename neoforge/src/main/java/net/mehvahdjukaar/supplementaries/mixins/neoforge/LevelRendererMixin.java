package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.neoforge.ModFluidsImpl;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @ModifyExpressionValue(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
    private static int supp$modifyLumiseneLight(int light, @Local(argsOnly = true) BlockState state) {
        if (light < 15 && state.is(ModFluids.LUMISENE_BLOCK.get())) {
            return Math.max(light, ModFluidsImpl.LUMISENE_FAKE_LIGHT_EMISSION);
        }
        return light;
    }


}
