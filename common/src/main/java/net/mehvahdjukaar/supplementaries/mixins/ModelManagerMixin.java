package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManagerClient;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

// super ugly. We need to run Book models reloader before model baking so we can bake extra models
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(method = "method_45895", at = @At("HEAD"))
    private static void supp$loadCustomBookModels(ResourceManager resourceManager, CallbackInfoReturnable<Map> cir) {
        try {
            PlaceableBookManagerClient.onEarlyPackLoad(resourceManager);
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed to load custom book models", e);
        }

    }
}
