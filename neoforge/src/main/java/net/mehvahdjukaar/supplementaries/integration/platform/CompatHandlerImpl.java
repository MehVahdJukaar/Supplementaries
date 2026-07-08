package net.mehvahdjukaar.supplementaries.integration.platform;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;

public class CompatHandlerImpl {

    public static void setupPlat() {
        if (CompatHandler.CREATE) CreateCompat.setup();
    }

    public static void initPlat() {
        if (CompatHandler.CREATE) CreateCompat.init();
    }
}
