package net.mehvahdjukaar.supplementaries.mixins;

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

    @Inject(
            method = "<init>",
            at = {@At(
                    value = "moonlight:INVOKE_UNRESTRICTED",
                    target = "Ljava/lang/Object;<init>()V",
                    remap = false
            )}
    )
    private void forceMipMapOn(int id, CallbackInfo ci) {
        MoonlightClient.setMipMap(true);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void forceMipMapOff(int id, CallbackInfo ci) {
        MoonlightClient.setMipMap(false);
    }
}
