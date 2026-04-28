package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

public class DecoBlocksCompat {

    @Contract
    @PlatformImpl
    public static boolean isPalisade(BlockState state) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void tryConvertingRopeChandelier(BlockState facingState, LevelAccessor worldIn, BlockPos facingPos) {
    }

    @PlatformImpl
    public static void init() {
    }

    @PlatformImpl
    public static void setupClient() {
        throw new AssertionError();
    }
}
