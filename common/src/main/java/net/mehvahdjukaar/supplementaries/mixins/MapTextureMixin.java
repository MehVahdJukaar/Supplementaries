package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightClient;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapTintColorsClient;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapTintColorsHandler;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MapRenderer.MapInstance.class)
public abstract class MapTextureMixin {


    @Shadow
    private MapItemSavedData data;

    @Shadow
    @Final
    private DynamicTexture texture;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V",
            shift = At.Shift.BEFORE), method = "updateTexture")
    public DynamicTexture supp$updateColoredTexture(MapRenderer.MapInstance instance, Operation<DynamicTexture> original) {
        MapTintColorsHandler.ColorData colorData = MapTintColorsHandler.getColorData(this.data);
        MapTintColorsClient.processTexture(colorData, this.texture.getPixels(), 0, 0, this.data.colors);
        MapLightHandler.LightData lightData = MapLightHandler.getLightData(this.data);
        MapLightClient.processTexture(lightData, this.texture.getPixels(), 0, 0, this.data.dimension);
        return original.call(instance);
    }


}