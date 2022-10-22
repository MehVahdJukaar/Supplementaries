package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class RedstoneDriverBlock extends DiodeBlock {
    public RedstoneDriverBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(FACING, POWERED);
    }

    @Override
    protected int getDelay(BlockState state) {
        return 0;
    }

    @Override
    protected int getInputSignal(Level world, BlockPos pos, BlockState state) {
        return super.getInputSignal(world, pos, state);
    }

    @Override
    protected int getAlternateSignal(LevelReader world, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        Direction direction1 = direction.getClockWise();
        Direction direction2 = direction.getCounterClockWise();
        return Math.max(this.getAlternateSignalAt(world, pos.relative(direction1), direction1), this.getAlternateSignalAt(world, pos.relative(direction2), direction2));
    }
}
