package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.world.entity.animal.Pig;

public class EnvironmentalCompat {

    @PlatformImpl
    public static boolean maybeCleanMuddyPig(Pig pig) {
        throw new AssertionError();
    }
}
