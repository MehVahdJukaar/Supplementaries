package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public interface ILavaAndWaterLoggable extends IBucketPickupHandler, ILiquidContainer {

    default boolean canPlaceLiquid(IBlockReader reader, BlockPos pos, BlockState state, Fluid fluid) {
        return (!state.getValue(BlockProperties.LAVALOGGED) && fluid == Fluids.LAVA)
                ||(!state.getValue(BlockStateProperties.WATERLOGGED) && fluid == Fluids.WATER);
    }

    default boolean placeLiquid(IWorld world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.getValue(BlockProperties.LAVALOGGED) && fluidState.getType() == Fluids.LAVA) {
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(BlockProperties.LAVALOGGED, Boolean.TRUE), 3);
                world.getLiquidTicks().scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }

            return true;
        }
        else if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE), 3);
                world.getLiquidTicks().scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }

            return true;
        }
        return false;
    }

    default Fluid takeLiquid(IWorld world, BlockPos pos, BlockState state) {
        if (state.getValue(BlockProperties.LAVALOGGED)) {
            world.setBlock(pos, state.setValue(BlockProperties.LAVALOGGED, Boolean.FALSE), 3);
            return Fluids.LAVA;
        }
        else if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE), 3);
            return Fluids.WATER;
        }

        return Fluids.EMPTY;

    }
}
