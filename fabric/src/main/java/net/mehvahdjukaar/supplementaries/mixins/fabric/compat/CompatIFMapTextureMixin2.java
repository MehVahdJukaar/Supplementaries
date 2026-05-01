package net.mehvahdjukaar.supplementaries.mixins.fabric.compat;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightClient;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapTintColorsClient;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapTintColorsHandler;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@OptionalMixin(value = "net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation.MixinMapRenderer_MapTexture")
@Mixin(value = MapRenderer.MapInstance.class, priority = 1500)
public class CompatIFMapTextureMixin2 {

    @Shadow
    private MapItemSavedData data;

    @TargetHandler(
            mixin = "net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation.MixinMapRenderer_MapTexture",
            name = "updateAtlasTexture"
    )
    @WrapOperation(method = "@MixinSquared:Handler",
            //require = 0,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;upload(IIIIIIIZZ)V"))
    public void supplementaries_IFupdateColoredTexture(NativeImage instance,
                                                       int level, int xOffset, int yOffset, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean mipmap, boolean autoClose,
                                                       Operation<Void> operation) {
        MapTintColorsHandler.ColorData colorData = MapTintColorsHandler.getColorData(this.data);
        MapTintColorsClient.processTexture(colorData, instance, xOffset, yOffset, this.data.colors);
        MapLightHandler.LightData lightData = MapLightHandler.getLightData(this.data);
        MapLightClient.processTexture(lightData, instance, xOffset, yOffset, this.data.dimension);

        MoonlightClient.setMipMap(true);
        mipmap = mipmap || MoonlightClient.isMapMipMap();
        operation.call(instance, level, xOffset, yOffset, unpackSkipPixels, unpackSkipRows, width, height, mipmap, autoClose);
        if (!autoClose && mipmap) GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        MoonlightClient.setMipMap(false);
    }

}
