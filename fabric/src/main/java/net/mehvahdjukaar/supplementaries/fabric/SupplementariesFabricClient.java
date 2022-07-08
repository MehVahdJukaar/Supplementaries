package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.supplementaries.SupplementariesClient;
import net.fabricmc.api.ClientModInitializer;

public class SupplementariesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SupplementariesClient.initClient();
        //TODO: add ISTERS
    }


}
