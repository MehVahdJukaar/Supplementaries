package net.mehvahdjukaar.supplementaries.compat;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.compat.botania.client.BotaniaCompatClient;
import net.mehvahdjukaar.supplementaries.compat.configured.CustomConfigSelectScreen;
import net.mehvahdjukaar.supplementaries.compat.decorativeblocks.DecoBlocksCompatClient;
import net.mehvahdjukaar.supplementaries.compat.flywheel.FlywheelPlugin;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CompatHandlerClient {

    public static void init(final FMLClientSetupEvent event) {

        if (CompatHandler.botania) BotaniaCompatClient.registerRenderLayers();
        if (CompatHandler.deco_blocks) DecoBlocksCompatClient.registerRenderLayers();
        //registers custom screen instead of default configured one
        if (CompatHandler.configured && RegistryConfigs.reg.CUSTOM_CONFIGURED_SCREEN.get()) {
            try {

                CustomConfigSelectScreen.registerScreen();
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("Failed to register custom configured screen: " + e);
            }
        }

        if (CompatHandler.flywheel) {
            FlywheelPlugin.registerInstances();
        }
    }
}
