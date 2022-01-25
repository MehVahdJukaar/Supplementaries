package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBeeGrowable {

    boolean getPollinated(World level, BlockPos pos, BlockState state);
}
