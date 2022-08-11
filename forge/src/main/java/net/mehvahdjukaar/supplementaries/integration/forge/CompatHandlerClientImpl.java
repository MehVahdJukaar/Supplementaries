package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.forge.configured.ModConfigSelectScreen;

public class CompatHandlerClientImpl {

    public static void init() {
        if (CompatHandler.configured && RegistryConfigs.CUSTOM_CONFIGURED_SCREEN.get()) {
            ModConfigSelectScreen.registerConfigScreen(Supplementaries.MOD_ID, ModConfigSelectScreen::new);
        }
    }
}
