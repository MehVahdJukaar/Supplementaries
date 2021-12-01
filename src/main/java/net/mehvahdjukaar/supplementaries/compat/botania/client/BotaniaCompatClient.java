package net.mehvahdjukaar.supplementaries.compat.botania.client;

import net.mehvahdjukaar.supplementaries.compat.botania.BotaniaCompatRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class BotaniaCompatClient {
    public static void registerRenderLayers() {
        RenderTypeLookup.setRenderLayer(BotaniaCompatRegistry.TATER_IN_A_JAR.get(), RenderType.cutout());

        ClientRegistry.bindTileEntityRenderer(BotaniaCompatRegistry.TATER_IN_A_JAR_TILE.get(), TaterInAJarTileRenderer::new);
    }
}
