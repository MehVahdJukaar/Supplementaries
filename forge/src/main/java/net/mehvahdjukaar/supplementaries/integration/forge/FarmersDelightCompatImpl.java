package net.mehvahdjukaar.supplementaries.integration.forge;

import vectorwing.farmersdelight.common.Configuration;

public class FarmersDelightCompatImpl {
    public static boolean isTomatoVineClimbingConfigOn() {
        return Configuration.ENABLE_TOMATO_VINE_CLIMBING_TAGGED_ROPES.get();
    }
}
