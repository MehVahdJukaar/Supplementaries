package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class RedstoneDriverBlock extends RedstoneDiodeBlock {
    public RedstoneDriverBlock(Properties properties) {
        super(properties);
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(FACING, POWERED);
    }

    @Override
    protected int getDelay(BlockState p_196346_1_) {
        return 0;
    }

    @Override
    protected int getInputSignal(World world, BlockPos pos, BlockState state) {
        return super.getInputSignal(world, pos, state);
    }

    @Override
    protected int getAlternateSignal(IWorldReader world, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        Direction direction1 = direction.getClockWise();
        Direction direction2 = direction.getCounterClockWise();
        return Math.max(this.getAlternateSignalAt(world, pos.relative(direction1), direction1), this.getAlternateSignalAt(world, pos.relative(direction2), direction2));
    }
}
