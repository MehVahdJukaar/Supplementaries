package net.mehvahdjukaar.supplementaries.client;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface GlobeRenderData {
    GlobeManager.Model getModel(boolean sepia);

    @Nullable
    ResourceLocation getTexture(boolean sepia);


}