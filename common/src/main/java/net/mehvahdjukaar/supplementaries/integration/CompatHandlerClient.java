package net.mehvahdjukaar.supplementaries.integration;


import dev.architectury.injectables.annotations.ExpectPlatform;

public class CompatHandlerClient {

    @ExpectPlatform
    public static void setup() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }
}
