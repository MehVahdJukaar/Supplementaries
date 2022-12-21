package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.DecoBlocksCompat;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.mehvahdjukaar.supplementaries.integration.FlywheelCompat;
import net.mehvahdjukaar.supplementaries.integration.forge.configured.ModConfigSelectScreen;

public class CompatHandlerClientImpl {

    public static void setup() {
        if (CompatHandler.CONFIGURED && RegistryConfigs.CUSTOM_CONFIGURED_SCREEN.get()) {
            ModConfigSelectScreen.registerConfigScreen(Supplementaries.MOD_ID, ModConfigSelectScreen::new);
        }
        if (CompatHandler.DECO_BLOCKS) {
            DecoBlocksCompat.setupClient();
        }
        if (CompatHandler.QUARK) {
            QuarkClientCompatImpl.setupClient();
        }
        if (CompatHandler.FARMERS_DELIGHT) {
            FarmersDelightCompat.setupClient();
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
