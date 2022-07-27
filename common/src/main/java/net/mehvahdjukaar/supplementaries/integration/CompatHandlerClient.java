package net.mehvahdjukaar.supplementaries.integration;


import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;

public class CompatHandlerClient {

    @ExpectPlatform
    public static void init() {
        if (CompatHandler.configured && RegistryConfigs.CUSTOM_CONFIGURED_SCREEN.get()) {
            try {

                //CustomConfigSelectScreen.registerScreen();
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("Failed to register custom configured screen: " + e);
            }
        }
        /*
        if (CompatHandler.botania) BotaniaCompatClient.registerRenderLayers();

        if (CompatHandler.deco_blocks) DecoBlocksCompatClient.registerRenderLayers();
        //registers custom screen instead of default configured one


        if (CompatHandler.flywheel) {
              FlywheelPlugin.registerInstances();
        }*/
    }

    public static void registerEntityRenderers(ClientPlatformHelper.EntityRendererEvent event) {
        // if (CompatHandler.botania) BotaniaCompatClient.registerEntityRenderers(event);
    }
}
