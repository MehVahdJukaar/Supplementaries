package net.mehvahdjukaar.supplementaries.integration;


import dev.architectury.injectables.annotations.ExpectPlatform;

public class CompatHandlerClient {

    @ExpectPlatform
    public static void loaderSpecificSetup() {
        throw new AssertionError();
    }

    public static void setup() {
        loaderSpecificSetup();
        if (CompatHandler.DECO_BLOCKS) {
            DecoBlocksCompat.setupClient();
        }
        if (CompatHandler.FARMERS_DELIGHT) {
            FarmersDelightCompat.setupClient();
        }
        if (CompatHandler.CAVE_ENHANCEMENTS) {
            CaveEnhancementsCompat.setupClient();
        }
        if (CompatHandler.BUZZIER_BEES) {
            BuzzierBeesCompat.setupClient();
        }
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }
}
