package net.mehvahdjukaar.supplementaries.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ModArguments {

    public static void init() {
        registerArguments();
    }

    @ExpectPlatform
    private static void registerArguments() {
        throw new AssertionError();
    }
}
