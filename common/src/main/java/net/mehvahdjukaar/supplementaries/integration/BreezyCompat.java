package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BreezyCompat {

    @ExpectPlatform
    public static float getWindAngle(BlockPos pos, Level level) {
        throw new AssertionError();
    }
}
