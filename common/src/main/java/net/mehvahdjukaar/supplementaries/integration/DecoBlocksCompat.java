package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

public class DecoBlocksCompat {

    @Contract
    @ExpectPlatform
    public static boolean isPalisade(BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void tryConvertingRopeChandelier(BlockState facingState, LevelAccessor worldIn, BlockPos facingPos) {
    }

    @ExpectPlatform
    public static void init() {
    }

    @ExpectPlatform
    public static void setupClient(){
        throw  new AssertionError();
    }
}
