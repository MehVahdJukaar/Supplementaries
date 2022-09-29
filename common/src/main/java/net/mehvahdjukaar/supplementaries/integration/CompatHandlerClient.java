package net.mehvahdjukaar.supplementaries.integration;


import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;

public class CompatHandlerClient {

    @ExpectPlatform
    public static void setup() {
    }

    @ExpectPlatform
    public static void init() {
        if(CompatHandler.flywheel) FlywheelCompat.initialize();
    }
}
