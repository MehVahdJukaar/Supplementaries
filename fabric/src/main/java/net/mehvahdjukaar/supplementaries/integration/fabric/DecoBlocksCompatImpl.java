package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DecoBlocksCompatImpl {
    public static boolean isBrazier(Block b) {
        return false;
    }

    public static boolean canLightBrazier(BlockState state) {
        return false;
    }

    public static boolean isPalisade(BlockState state) {
        return false;
    }

    public static void tryConvertingRopeChandelier(BlockState facingState, LevelAccessor worldIn, BlockPos facingPos) {
    }
}
