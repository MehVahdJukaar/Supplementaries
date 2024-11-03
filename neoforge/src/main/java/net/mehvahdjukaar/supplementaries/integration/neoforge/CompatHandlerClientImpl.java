package net.mehvahdjukaar.supplementaries.integration.neoforge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.mehvahdjukaar.supplementaries.integration.FlywheelCompat;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.mehvahdjukaar.supplementaries.integration.neoforge.configured.ModConfigSelectScreen;

public class CompatHandlerClientImpl {

    public static void doSetup() {
        if (CompatHandler.CONFIGURED && ClientConfigs.General.CUSTOM_CONFIGURED_SCREEN.get()) {
            ModConfigSelectScreen.registerConfigScreen(Supplementaries.MOD_ID, ModConfigSelectScreen::new);
        }

        if (CompatHandler.QUARK) {
            QuarkClientCompat.setupClient();
        }

        if (CompatHandler.CREATE) {
            CreateCompat.setupClient();
        }
        if (CompatHandler.FLYWHEEL) {
            FlywheelCompat.setupClient();
        }
    }

    public static void init() {
        if (CompatHandler.QUARK) {
            QuarkClientCompat.initClient();
        }
    }

}
