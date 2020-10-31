package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.client.ClockBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.client.PedestalBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.client.WindVaneBlockTileRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(final FMLClientSetupEvent event){

        //planter
        RenderTypeLookup.setRenderLayer(Registry.PLANTER.get(), RenderType.getCutout());
        //clock
        RenderTypeLookup.setRenderLayer(Registry.CLOCK_BLOCK.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        //pedestal
        RenderTypeLookup.setRenderLayer(Registry.PEDESTAL.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        //wind vane
        RenderTypeLookup.setRenderLayer(Registry.WIND_VANE.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);





    }


}
