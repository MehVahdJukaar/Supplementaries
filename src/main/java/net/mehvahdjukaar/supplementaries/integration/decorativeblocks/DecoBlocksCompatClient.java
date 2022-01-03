package net.mehvahdjukaar.supplementaries.integration.decorativeblocks;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

public class DecoBlocksCompatClient {

    public static void registerRenderLayers() {
        if (DecoBlocksCompatRegistry.CHANDELIER_ROPE != null)
            ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.CHANDELIER_ROPE.get(), RenderType.cutout());
        if (DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE != null)
            ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE.get(), RenderType.cutout());
        if (CompatHandler.deco_blocks_abnormals) {
            if (DecoBlocksCompatRegistry.ENDER_CHANDELIER_ROPE != null)
                ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.ENDER_CHANDELIER_ROPE.get(), RenderType.cutout());
        }
        if (CompatHandler.much_more_mod_compat) {
            if (DecoBlocksCompatRegistry.GLOW_CHANDELIER_ROPE != null)
                ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.GLOW_CHANDELIER_ROPE.get(), RenderType.cutout());
        }
    }
}
