package net.mehvahdjukaar.supplementaries.integration;


import net.mehvahdjukaar.candlelight.api.PlatformImpl;

public class CreateCompat {

    @PlatformImpl
    public static void setup() {
        throw new AssertionError();
    }


    @PlatformImpl
    public static void setupClient() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void init() {
        throw new AssertionError();
    }


}
