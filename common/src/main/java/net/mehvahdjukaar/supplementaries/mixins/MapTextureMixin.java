package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightHandler;
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
    public DynamicTexture updateColoredTexture(MapRenderer.MapInstance instance, Operation<DynamicTexture> original) {
        ColoredMapHandler.getColorData(this.data).processTexture( this.texture.getPixels(), 0, 0, this.data.colors);
        MapLightHandler.getLightData(this.data).processTexture(this.texture.getPixels(), 0, 0, this.data.dimension);
        return original.call(instance);
    }


}