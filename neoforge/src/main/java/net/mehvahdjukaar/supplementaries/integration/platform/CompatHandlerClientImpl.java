package net.mehvahdjukaar.supplementaries.integration.platform;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;

public class CompatHandlerClientImpl {

    public static void doSetup() {
        // config screens are handled natively by Moonlight (it auto-registers the config screen factory for every
        // mod that uses its ConfigBuilder), so no bespoke Configured integration is needed here anymore.
        if (CompatHandler.CREATE) CreateCompat.setupClient();
    }

    public static void doInit() {

    }

}
