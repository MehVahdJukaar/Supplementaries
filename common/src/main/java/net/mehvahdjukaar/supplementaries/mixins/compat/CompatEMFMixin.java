package net.mehvahdjukaar.supplementaries.mixins.compat;

import net.mehvahdjukaar.supplementaries.client.IModelPartExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.jem_objects.EMFPartData;
import traben.entity_model_features.models.parts.EMFModelPartCustom;

@Pseudo
@Mixin(EMFModelPartCustom.class)
public class CompatEMFMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void supp$onInit(EMFPartData emfPartData, int variant, String part, String id, CallbackInfo ci) {
        ((IModelPartExtension) this).supp$setDimensions(emfPartData.textureSize[0], emfPartData.textureSize[1]);
    }
}
