package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class DecoBlocksCompatClient {
    public static void registerRenderLayers(){
        RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.CHANDELIER_ROPE, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE, RenderType.cutout());
        if(CompatHandler.deco_blocks_abnormals){
            RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.ENDER_CHANDELIER_ROPE, RenderType.cutout());
        }
        if(CompatHandler.much_more_mod_compat){
            RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.GLOW_CHANDELIER_ROPE, RenderType.cutout());
        }
    }
}
