package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ClientEventsFabric;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ServerEventsFabric;
import net.mehvahdjukaar.supplementaries.reg.ModSetup;

public class SupplementariesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Supplementaries.commonInit();

        ServerEventsFabric.init();

        if (PlatformHelper.getEnv().isClient()) {
            VibeChecker.checkVibe();
            ClientEventsFabric.init();
            SupplementariesFabricClient.clientInitAndSetup();
        }

        FabricSetupCallbacks.COMMON_SETUP.add(ModSetup::setup);
        FabricSetupCallbacks.COMMON_SETUP.add(ModSetup::asyncSetup);

    }



}
