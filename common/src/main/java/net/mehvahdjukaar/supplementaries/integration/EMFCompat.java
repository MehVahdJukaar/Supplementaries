package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.Nullable;
import traben.entity_model_features.models.IEMFModel;

public class EMFCompat {

    @Nullable
    public static ModelPart getFirstEMFModelPart(Model model) {
        if (model instanceof IEMFModel em && em.emf$isEMFModel()) {
            for (var part : em.emf$getEMFRootModel().getAllVanillaPartsEMF()) {
                for (var child : part.getAllEMFCustomChildren()) {
                    return child;
                }
            }
        }
        return null;
    }
}
