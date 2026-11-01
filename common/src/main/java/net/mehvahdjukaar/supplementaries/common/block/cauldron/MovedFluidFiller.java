package net.mehvahdjukaar.supplementaries.common.block.cauldron;

import net.mehvahdjukaar.supplementaries.common.block.IFluidFillableOnMove;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.AmendmentsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovedFluidFiller {

    public static FluidState getFluidLeftBehind(BlockState state, Level level, BlockPos pos) {
        if (state.is(ModTags.WATER_HOLDER)) return Fluids.EMPTY.defaultFluidState();
        return level.getFluidState(pos);
    }

    //must run before anything is moved or destroyed
    public static Map<BlockPos, FluidState> snapshotFluidsLeftBehind(Level level, List<BlockPos> toPush, List<BlockPos> toDestroy) {
        Map<BlockPos, FluidState> fluids = new HashMap<>();
        if (!CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get()) return fluids;
        for (BlockPos pos : toPush) {
            FluidState fluid = getFluidLeftBehind(level.getBlockState(pos), level, pos);
            if (!fluid.isEmpty()) fluids.put(pos.immutable(), fluid);
        }
        for (BlockPos pos : toDestroy) {
            FluidState fluid = level.getFluidState(pos);
            if (!fluid.isEmpty()) fluids.put(pos.immutable(), fluid);
        }
        return fluids;
    }

    public static FluidState getSnapshotFluid(Map<BlockPos, FluidState> fluids, BlockPos pos) {
        return fluids.getOrDefault(pos, Fluids.EMPTY.defaultFluidState());
    }

    public static BlockState getStateMovedInto(BlockState movedState, Level level, BlockPos destPos, FluidState destFluid) {
        if (movedState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            boolean waterlogged = destFluid.is(Fluids.WATER)
                    || (movedState.is(ModTags.WATER_HOLDER) && movedState.getValue(BlockStateProperties.WATERLOGGED));
            return movedState.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        if (destFluid.isEmpty() || !CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get()) return movedState;

        Block b = movedState.getBlock();
        if (b instanceof IFluidFillableOnMove fillable) {
            return fillable.getStateWhenMovedIntoFluid(movedState, destFluid, level, destPos);
        }
        if (b instanceof AbstractCauldronBlock) {
            if (destFluid.is(Fluids.WATER) && (movedState.is(Blocks.CAULDRON) || movedState.is(Blocks.WATER_CAULDRON))) {
                return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
            } else if (destFluid.is(Fluids.LAVA) && (movedState.is(Blocks.CAULDRON) || movedState.is(Blocks.LAVA_CAULDRON))) {
                return Blocks.LAVA_CAULDRON.defaultBlockState();
            } else if (CompatHandler.AMENDMENTS) {
                // Amendments stores modded fluids in a block entity
                return AmendmentsCompat.fillCauldronWithFluid(level, destPos, movedState, destFluid);
            }
        }
        return movedState;
    }

    private static boolean isWaterlogged(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    private static boolean needsPostPlacement(BlockState movedState) {
        if (movedState.getBlock() instanceof IFluidFillableOnMove) return true;
        if (isWaterlogged(movedState)) return true;
        return CompatHandler.AMENDMENTS && AmendmentsCompat.isModCauldron(movedState);
    }

    public static void markForPostPlacement(BlockEntity movingBe, BlockState movedState, FluidState destFluid) {
        if (movingBe instanceof ICarryingMovingPiston carrying && needsPostPlacement(movedState)) {
            carrying.supp$setMovedFluid(destFluid);
        }
    }

    public static void applyPostPlacement(Level level, BlockPos destPos, BlockState movedState, FluidState destFluid) {
        if (level.isClientSide) return;
        BlockState placed = level.getBlockState(destPos);
        // moving pistons always drop waterlogged on landing
        boolean lostWater = isWaterlogged(movedState)
                && placed.is(movedState.getBlock()) && !placed.getValue(BlockStateProperties.WATERLOGGED);
        if (lostWater) {
            level.setBlock(destPos, placed.setValue(BlockStateProperties.WATERLOGGED, true), 3);
            level.scheduleTick(destPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            return;
        }

        if (destFluid.isEmpty() || !CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get()) return;
        if (placed.getBlock() instanceof IFluidFillableOnMove fillable) {
            fillable.onMovedIntoFluid(level, destPos, placed, destFluid);
        } else if (CompatHandler.AMENDMENTS && AmendmentsCompat.isModCauldron(placed)) {
            AmendmentsCompat.fillMovedCauldronTile(level, destPos, destFluid);
        }
    }
}
