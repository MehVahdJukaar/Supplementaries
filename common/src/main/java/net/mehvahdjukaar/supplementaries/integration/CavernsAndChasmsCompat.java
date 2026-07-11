package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.world.entity.Entity;

public class CavernsAndChasmsCompat {

    @PlatformImpl
    public static boolean maybeCleanDirtyRat(Entity entity) {
        throw new AssertionError();
    }
}
