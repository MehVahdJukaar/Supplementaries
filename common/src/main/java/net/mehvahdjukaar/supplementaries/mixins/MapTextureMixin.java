package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.misc.ColoredMapHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.MapInstance.class)
public abstract class MapTextureMixin {


    @Shadow private MapItemSavedData data;

    @Shadow @Final private DynamicTexture texture;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V",
    shift = At.Shift.BEFORE), method = "updateTexture")
    public void updateTintColors(CallbackInfo ci) {
        ColoredMapHandler.getColorData(this.data).processTexture(this.texture, this.data.colors);
    }


}