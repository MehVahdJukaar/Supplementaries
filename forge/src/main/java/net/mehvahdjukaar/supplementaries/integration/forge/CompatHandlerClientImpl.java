package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.DecoBlocksClientCompat;
import net.mehvahdjukaar.supplementaries.integration.forge.configured.ModConfigSelectScreen;

public class CompatHandlerClientImpl {

    public static void setup() {
        if (CompatHandler.CONFIGURED && RegistryConfigs.CUSTOM_CONFIGURED_SCREEN.get()) {
            ModConfigSelectScreen.registerConfigScreen(Supplementaries.MOD_ID, ModConfigSelectScreen::new);
        }
        if (CompatHandler.DECO_BLOCKS) {
            DecoBlocksClientCompat.registerRenderLayers();
        }
        if (CompatHandler.QUARK) {
            QuarkClientCompatImpl.registerRenderLayers();
        }
        if (CompatHandler.FARMERS_DELIGHT) {
            FarmersDelightCompatImpl.registerRenderLayers();
        }
    }

    public static void init() {
        if (CompatHandler.QUARK) {
            QuarkClientCompatImpl.init();
        }
    }

}
