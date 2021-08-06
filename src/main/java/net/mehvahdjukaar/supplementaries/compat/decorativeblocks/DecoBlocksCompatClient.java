package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class DecoBlocksCompatClient {
    public static void registerRenderLayers(){
        RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.CHANDELIER_ROPE, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE, RenderType.cutout());
        if(CompatHandler.endergetic){
            RenderTypeLookup.setRenderLayer(DecoBlocksCompatRegistry.getEnderRopeChandelier(), RenderType.cutout());
        }
    }
}
