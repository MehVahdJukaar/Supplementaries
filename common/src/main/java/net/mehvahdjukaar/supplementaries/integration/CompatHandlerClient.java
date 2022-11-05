package net.mehvahdjukaar.supplementaries.integration;


import dev.architectury.injectables.annotations.ExpectPlatform;

public class CompatHandlerClient {

    @ExpectPlatform
    public static void setup() {
    }

    @ExpectPlatform
    public static void init() {
        if(CompatHandler.FLYWHEEL) FlywheelCompat.initialize();
    }
}
