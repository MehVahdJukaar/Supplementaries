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
        FabricSetupCallbacks.COMMON_SETUP.add(Supplementaries::commonSetup);

        if(PlatformHelper.getEnv().isClient()){
            ClientEventsFabric.init();
            FabricSetupCallbacks.CLIENT_SETUP.add(SupplementariesFabricClient::clientSetup);
        }

    }
}
