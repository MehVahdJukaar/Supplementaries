package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public interface ILavaAndWaterLoggable extends BucketPickup, LiquidBlockContainer {

    default boolean canPlaceLiquid(BlockGetter reader, BlockPos pos, BlockState state, Fluid fluid) {
        return (!state.getValue(ModBlockProperties.LAVALOGGED) && fluid == Fluids.LAVA)
                || (!state.getValue(BlockStateProperties.WATERLOGGED) && fluid == Fluids.WATER);
    }

    default boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.getValue(ModBlockProperties.LAVALOGGED) && fluidState.getType() == Fluids.LAVA) {
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(ModBlockProperties.LAVALOGGED, Boolean.TRUE), 3);
                world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }

            return true;
        } else if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE), 3);
                world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }

            return true;
        }
        return false;
    }

    default ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        if (pState.getValue(BlockStateProperties.WATERLOGGED)) {
            pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE), 3);
            if (!pState.canSurvive(pLevel, pPos)) {
                pLevel.destroyBlock(pPos, true);
            }

            return new ItemStack(Items.WATER_BUCKET);
        } else if (pState.getValue(ModBlockProperties.LAVALOGGED)) {
            pLevel.setBlock(pPos, pState.setValue(ModBlockProperties.LAVALOGGED, Boolean.FALSE), 3);
            if (!pState.canSurvive(pLevel, pPos)) {
                pLevel.destroyBlock(pPos, true);
            }

            return new ItemStack(Items.LAVA_BUCKET);
        }
        return ItemStack.EMPTY;
    }

    default Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }

}
