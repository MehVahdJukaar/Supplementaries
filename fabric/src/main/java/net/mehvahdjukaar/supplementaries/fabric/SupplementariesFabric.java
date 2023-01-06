package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ClientEventsFabric;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ServerEventsFabric;

public class SupplementariesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Supplementaries.commonInit();

        ServerEventsFabric.init();

        if (PlatformHelper.getEnv().isClient()) {
            ClientEventsFabric.init();
            FabricSetupCallbacks.CLIENT_SETUP.add(SupplementariesFabricClient::clientInit);
            throwIfFabricRenderingAPIHasBeenNuked();
        }

    }

    //I hate this. I've got to do what I've got to do. Cant stand random reports anymore
    public static void throwIfFabricRenderingAPIHasBeenNuked() {
        if (PlatformHelper.isModLoaded("sodium") && !PlatformHelper.isModLoaded("indium")) {
            throw new IllegalStateException("You seem to have installed Sodium which breaks fabric rendering API." +
                    "To fix you must install Indium as Supplementaries, as many other mods, rely on said API");
        }
    }
}
