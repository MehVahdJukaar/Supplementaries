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

/**
 * Shared logic for filling a block that is being moved (pushed by a piston or dragged by a
 * pulley/rope) into a fluid source. Built-in support covers vanilla cauldrons and (when installed)
 * Amendments liquid cauldrons; other mods opt in via {@link IFluidFillableOnMove}. Gated behind
 * {@link CommonConfigs.Tweaks#MOVED_CAULDRON_FILLING}.
 * <p>
 * Filling is two-phase: {@link #fillIfMovedIntoFluid} computes the state that should land (baked
 * into the moving-block animation), and {@link #applyPostPlacement} populates any block-entity data
 * once that state has actually been placed.
 */
public class MovedFluidFiller {

    /**
     * Whether this block reacts to being moved into a fluid at all (built-in cauldron or opted-in).
     */
    public static boolean reactsToFluid(BlockState state) {
        Block b = state.getBlock();
        return b instanceof IFluidFillableOnMove || b instanceof AbstractCauldronBlock;
    }

    /**
     * Computes the state a moved block should turn into when it lands where {@code destFluid} used
     * to be. Returns the unchanged state if the feature is off, the block doesn't react, or the
     * fluid isn't fillable. When the placed block needs block-entity data, follow up with
     * {@link #applyPostPlacement} once the returned state is actually in the world.
     */
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
                // Amendments stores modded fluids in a block entity, so the state alone isn't enough:
                // this picks the right cauldron block, and applyPostPlacement() fills the tank after.
                return AmendmentsCompat.fillCauldronWithFluid(level, destPos, movedState, destFluid);
            }
        }
        return movedState;
    }

    /**
     * Whether {@link #applyPostPlacement} would do anything for this just-placed state — i.e. the
     * block needs block-entity data set up after landing. Vanilla water/lava cauldrons are fully
     * described by their state and don't.
     */
    public static boolean needsPostPlacement(BlockState placedState) {
        if (placedState.getBlock() instanceof IFluidFillableOnMove) return true;
        return CompatHandler.AMENDMENTS && AmendmentsCompat.isModCauldron(placedState);
    }

    /**
     * Second phase: once the filled state from {@link #fillIfMovedIntoFluid} has been placed at
     * {@code destPos}, populate any block-entity data from {@code destFluid}. Safe to call for any
     * placed block — it no-ops unless the block actually needs it.
     */
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
