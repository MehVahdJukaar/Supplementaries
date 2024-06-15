package net.mehvahdjukaar.supplementaries.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;

public class ModArguments {

    public static void init() {
        if(PlatHelper.getPlatform().isFabric()) registerArguments();
    }

    @ExpectPlatform
    public static void registerArguments() {
        throw new AssertionError();
    }
}
