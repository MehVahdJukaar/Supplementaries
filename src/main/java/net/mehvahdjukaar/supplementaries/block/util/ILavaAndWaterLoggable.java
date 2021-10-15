package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

public interface ILavaAndWaterLoggable extends BucketPickup, LiquidBlockContainer {

    default boolean canPlaceLiquid(BlockGetter reader, BlockPos pos, BlockState state, Fluid fluid) {
        return (!state.getValue(BlockProperties.LAVALOGGED) && fluid == Fluids.LAVA)
                ||(!state.getValue(BlockStateProperties.WATERLOGGED) && fluid == Fluids.WATER);
    }

    default boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
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

    default Fluid takeLiquid(LevelAccessor world, BlockPos pos, BlockState state) {
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
