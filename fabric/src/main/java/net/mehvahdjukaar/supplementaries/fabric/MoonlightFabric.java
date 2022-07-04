package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.supplementaries.SupplementariesNewInit;

public class MoonlightFabric implements ModInitializer {

    public static final String MOD_ID = SupplementariesNewInit.MOD_ID;

    @Override
    public void onInitialize() {

        SupplementariesNewInit.commonInit();
        SupplementariesNewInit.commonRegistration();
        SupplementariesNewInit.commonSetup();
    }


}
