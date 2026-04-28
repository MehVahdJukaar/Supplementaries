package net.mehvahdjukaar.supplementaries.integration.platform;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.platform.configured.ModConfigSelectScreen;

public class CompatHandlerClientImpl {

    public static void doSetup() {
        if (CompatHandler.CONFIGURED && ClientConfigs.General.CUSTOM_CONFIGURED_SCREEN.get()) {
            ModConfigSelectScreen.registerConfigScreen(Supplementaries.MOD_ID, ModConfigSelectScreen::new);
        }
    }

    public static void doInit() {

    }

}
