package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IAnalogRotatable {

    void rotateAnalog(BlockState state, Level level, BlockPos pos, Direction face, boolean ccw, float speed);

    boolean canRotateAnalog(BlockState state, Level level, BlockPos pos, Direction face);
}
