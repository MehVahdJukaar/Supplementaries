package net.mehvahdjukaar.supplementaries.client;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface GlobeRenderData {
    GlobeManager.Model getModel(boolean sepia);

    @NotNull
    ResourceLocation getTexture(boolean sepia);

}