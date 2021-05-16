package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class DecoBlocksCompatClient {
    public static void registerRenderLayers(){
        RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.CHANDELIER_ROPE, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE, RenderType.cutout());
    }
}
