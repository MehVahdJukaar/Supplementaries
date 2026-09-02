package net.mehvahdjukaar.supplementaries.common.block.cauldron;

import net.mehvahdjukaar.supplementaries.common.block.IFluidFillableOnMove;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.AmendmentsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class MovedFluidFiller {

    //TODO: remove
    public static boolean reactsToFluid(BlockState state) {
        Block b = state.getBlock();
        return b instanceof IFluidFillableOnMove || b instanceof AbstractCauldronBlock;
    }

    public static BlockState fillIfMovedIntoFluid(BlockState movedState, Level level, BlockPos destPos, FluidState destFluid) {
        if (!CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get()) return movedState;

        Block b = movedState.getBlock();
        if (b instanceof IFluidFillableOnMove fillable) {
            return fillable.getStateWhenMovedIntoFluid(movedState, destFluid, level, destPos);
        }
        if (b instanceof AbstractCauldronBlock) {
            if (destFluid.is(Fluids.WATER) && movedState.is(Blocks.CAULDRON) || movedState.is(Blocks.WATER_CAULDRON)) {
                return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
            } else if (destFluid.is(Fluids.LAVA) && movedState.is(Blocks.CAULDRON) || movedState.is(Blocks.LAVA_CAULDRON)) {
                return Blocks.LAVA_CAULDRON.defaultBlockState();
            } else if (CompatHandler.AMENDMENTS) {
                // Amendments stores modded fluids in a block entity
                return AmendmentsCompat.fillCauldronWithFluid(level, destPos, movedState, destFluid);
            }
        }
        return movedState;
    }

    public static boolean needsPostPlacement(BlockState placedState) {
        if (placedState.getBlock() instanceof IFluidFillableOnMove) return true;
        return CompatHandler.AMENDMENTS && AmendmentsCompat.isModCauldron(placedState);
    }

    public static void applyPostPlacement(Level level, BlockPos destPos, FluidState destFluid) {
        if (!CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get()) return;
        if (destFluid.isEmpty()) return;

        BlockState placed = level.getBlockState(destPos);
        if (placed.getBlock() instanceof IFluidFillableOnMove fillable) {
            fillable.onMovedIntoFluid(level, destPos, placed, destFluid);
        } else if (CompatHandler.AMENDMENTS && AmendmentsCompat.isModCauldron(placed)) {
            AmendmentsCompat.fillMovedCauldronTile(level, destPos, destFluid);
        }
    }
}
