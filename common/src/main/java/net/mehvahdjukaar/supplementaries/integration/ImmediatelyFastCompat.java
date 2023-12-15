package net.mehvahdjukaar.supplementaries.integration;

import net.raphimc.immediatelyfastapi.BatchingAccess;
import net.raphimc.immediatelyfastapi.ImmediatelyFastApi;

public class ImmediatelyFastCompat {

    public static void startBatching(){
        BatchingAccess batching = ImmediatelyFastApi.getApiImpl().getBatching();
        batching.beginHudBatching();
    }

    public static void endBatching(){
        ImmediatelyFastApi.getApiImpl().getBatching().endHudBatching();
    }
}
