package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

public class FlowerPotUtils {
    //server side is tag driven
    public static void setup() {
        registerFlowerPots();
    }

    @Contract
    @PlatformImpl
    public static Block getEmptyPot(FlowerPotBlock fullPot) {
        throw new AssertionError();
    }

    @Contract
    @PlatformImpl
    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isEmptyPot(Block b) {
        throw new AssertionError();
    }

    @ApiStatus.Internal
    //move to forge
    @PlatformImpl
    public static void registerFlowerPots() {
        throw new AssertionError();
    }

}
