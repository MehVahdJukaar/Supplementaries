package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class BumblezoneCompat {

    public static void turnToSugarWater(Level level, BlockPos pos) {

        Set<BlockPos> set = new HashSet<>();
        set.add(pos);
        recursiveGatherPositions(level, pos, 0, 3, set);

        set.forEach(waterPos -> level.setBlock(waterPos, CompatObjects.SUGAR_WATER.get().defaultBlockState(), 3));
    }

    private static void recursiveGatherPositions(Level level, BlockPos position, int depth, int maxDepth, Set<BlockPos> waterPos) {
        if (depth == maxDepth) return;

        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for (Direction facing : Direction.values()) {
            neighborPos.set(position).move(facing);
            BlockState neighborBlock = level.getBlockState(neighborPos);

            if (neighborBlock.is(Blocks.WATER) && neighborBlock.getFluidState().isSource()) {
                waterPos.add(neighborPos.immutable());
                recursiveGatherPositions(level, neighborPos, depth + 1, maxDepth, waterPos);
            }
        }
    }
}
