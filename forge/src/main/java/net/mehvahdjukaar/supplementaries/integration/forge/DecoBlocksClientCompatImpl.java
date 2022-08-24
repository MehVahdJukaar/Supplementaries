package net.mehvahdjukaar.supplementaries.integration.forge;


import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.client.renderer.RenderType;

public class DecoBlocksClientCompatImpl {

    public static void registerRenderLayers() {
        if (DecoBlocksCompatImpl.CHANDELIER_ROPE != null)
            ClientPlatformHelper.registerRenderType(DecoBlocksCompatImpl.CHANDELIER_ROPE.get(), RenderType.cutout());
        if (DecoBlocksCompatImpl.SOUL_CHANDELIER_ROPE != null)
            ClientPlatformHelper.registerRenderType(DecoBlocksCompatImpl.SOUL_CHANDELIER_ROPE.get(), RenderType.cutout());
        if (CompatHandler.deco_blocks_abnormals) {
            if (DecoBlocksCompatImpl.ENDER_CHANDELIER_ROPE != null)
                ClientPlatformHelper.registerRenderType(DecoBlocksCompatImpl.ENDER_CHANDELIER_ROPE.get(), RenderType.cutout());
        }
        if (CompatHandler.much_more_mod_compat) {
            if (DecoBlocksCompatImpl.GLOW_CHANDELIER_ROPE != null)
                ClientPlatformHelper.registerRenderType(DecoBlocksCompatImpl.GLOW_CHANDELIER_ROPE.get(), RenderType.cutout());
        }
    }
}
