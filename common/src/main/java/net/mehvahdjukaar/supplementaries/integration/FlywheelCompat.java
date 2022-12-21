package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class FlywheelCompat {

    @ExpectPlatform
    public static void setupClient() {
        throw new AssertionError();
    }
}
