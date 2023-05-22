package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.DecoBlocksCompat;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.mehvahdjukaar.supplementaries.integration.FlywheelCompat;
import net.mehvahdjukaar.supplementaries.integration.forge.configured.ModConfigSelectScreen;

public class CompatHandlerClientImpl {

    public static void loaderSpecificSetup() {
        if (CompatHandler.CONFIGURED && ClientConfigs.General.CUSTOM_CONFIGURED_SCREEN.get()) {
            ModConfigSelectScreen.registerConfigScreen(Supplementaries.MOD_ID, ModConfigSelectScreen::new);
        }
        if (CompatHandler.QUARK) {
            QuarkClientCompatImpl.setupClient();
        }

        if(CompatHandler.CREATE){
            CreateCompatImpl.setupClient();
        }
        if(CompatHandler.FLYWHEEL){
            FlywheelCompat.setupClient();
        }
    }

    public static void init() {
        if (CompatHandler.QUARK) {
            QuarkClientCompatImpl.initClient();
        }
    }

}
