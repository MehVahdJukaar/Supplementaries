package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

public class DecoBlocksCompatClient {
    public static void registerRenderLayers(){
        ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.CHANDELIER_ROPE, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE, RenderType.cutout());
        if(CompatHandler.deco_blocks_abnormals){
            ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.ENDER_CHANDELIER_ROPE, RenderType.cutout());
        }
        if(CompatHandler.much_more_mod_compat){
            ItemBlockRenderTypes.setRenderLayer(DecoBlocksCompatRegistry.GLOW_CHANDELIER_ROPE, RenderType.cutout());
        }
    }
}
