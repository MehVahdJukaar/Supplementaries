package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TFCompat {

    @PlatformImpl
    public static BlockState tryRotateHollowLog(BlockState state, Direction face) {
        throw new AssertionError();
    }
}
