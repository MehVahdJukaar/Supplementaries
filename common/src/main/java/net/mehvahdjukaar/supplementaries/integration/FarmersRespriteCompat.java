package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FarmersRespriteCompat {

    @ExpectPlatform
    public static IntegerProperty getWaterLevel() {
        throw new AssertionError();

    }

    @ExpectPlatform
    public static boolean isKettle(BlockState block) {
        throw new AssertionError();
    }
}
