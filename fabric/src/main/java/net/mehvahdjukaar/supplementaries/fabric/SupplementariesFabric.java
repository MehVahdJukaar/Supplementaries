package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.fabric.MLFabricSetupCallbacks;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ClientEventsFabric;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ServerEventsFabric;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.reg.ModSetup;

public class SupplementariesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Supplementaries.commonInit();

        ServerEventsFabric.init();

        if (PlatHelper.getPhysicalSide().isClient()) {
            VibeChecker.checkVibe();
            ClientEventsFabric.init();
            SupplementariesFabricClient.clientInitAndSetup();
        }

        MLFabricSetupCallbacks.COMMON_SETUP.add(ModSetup::setup);
        MLFabricSetupCallbacks.COMMON_SETUP.add(ModSetup::asyncSetup);

    }
}
