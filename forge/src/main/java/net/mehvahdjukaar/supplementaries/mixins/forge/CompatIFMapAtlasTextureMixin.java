package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MapAtlasTexture.class)
public class CompatIFMapAtlasTextureMixin {

    @WrapOperation(
            method = {"<init>"},
            require = 0,
            at = {@At(
                    value = "NEW",
                    target = "(IIZ)Lnet/minecraft/client/renderer/texture/DynamicTexture;",
                    remap = false
            )}
    )
    private DynamicTexture forceMipMapOn(int width, int height, boolean useCalloc, Operation<DynamicTexture> original) {
        MoonlightClient.setMipMap(true);
        var t = original.call(width, height, useCalloc);
        MoonlightClient.setMipMap(false);
        return t;
    }
}
