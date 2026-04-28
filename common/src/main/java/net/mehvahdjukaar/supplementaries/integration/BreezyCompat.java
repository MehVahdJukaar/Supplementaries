package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BreezyCompat {

    @PlatformImpl
    public static float getWindAngle(BlockPos pos, Level level) {
        throw new AssertionError();
    }
}
