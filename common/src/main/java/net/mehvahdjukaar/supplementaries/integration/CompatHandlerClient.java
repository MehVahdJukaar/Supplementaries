package net.mehvahdjukaar.supplementaries.integration;


import dev.architectury.injectables.annotations.ExpectPlatform;

import static net.mehvahdjukaar.supplementaries.integration.CompatHandler.*;

public class CompatHandlerClient {

    @ExpectPlatform
    public static void doSetup() {
        throw new AssertionError();
    }

    public static void setup() {
        doSetup();
        if (DECO_BLOCKS) DecoBlocksCompat.setupClient();
        if (FARMERS_DELIGHT) FarmersDelightCompat.setupClient();
        if (CAVE_ENHANCEMENTS) CaveEnhancementsCompat.setupClient();
        if (BUZZIER_BEES) BuzzierBeesCompat.setupClient();
        if (INFERNALEXP) InfernalExpCompat.setupClient();
        if (ARCHITECTS_PALETTE) ArchitectsPalCompat.setupClient();
        if (ENDERGETIC) EndergeticCompat.setupClient();
        if (QUARK) QuarkClientCompat.setupClient();
        if (CREATE) CreateCompat.setupClient();
        if (FLYWHEEL) FlywheelCompat.setupClient();

    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }
}
