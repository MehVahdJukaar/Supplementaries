package net.mehvahdjukaar.supplementaries.integration;


import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CompatHandlerClient {

    public static void init(final FMLClientSetupEvent event) {

        /*
        if (CompatHandler.quark) {
            QuarkPlugin.registerTooltipComponent();
        }
        if (CompatHandler.botania) BotaniaCompatClient.registerRenderLayers();

        if (CompatHandler.deco_blocks) DecoBlocksCompatClient.registerRenderLayers();
        //registers custom screen instead of default configured one
        if (CompatHandler.configured && RegistryConfigs.Reg.CUSTOM_CONFIGURED_SCREEN.get()) {
            try {
                 CustomConfigSelectScreen.registerScreen();
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("Failed to register custom configured screen: " + e);
            }
        }

        if (CompatHandler.flywheel) {
              FlywheelPlugin.registerInstances();
        }*/
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
       // if (CompatHandler.botania) BotaniaCompatClient.registerEntityRenderers(event);
    }
}
