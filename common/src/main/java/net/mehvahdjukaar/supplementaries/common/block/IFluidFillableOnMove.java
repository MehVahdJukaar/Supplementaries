package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Implement on a block that should fill when a piston or pulley moves it into a fluid source, the
 * way a cauldron does. Only fires with the {@code moved_cauldron_filling} tweak on. Vanilla and
 * Amendments cauldrons are handled built in and don't need this.
 */
public interface IFluidFillableOnMove {

    /**
     * Decides the state that lands. For animated moves this is the state the moving block animation
     * carries, so any visible change belongs here.
     *
     * @param destPos the position the block is landing at, still mid animation for piston moves
     * @return the state to place, or {@code movedState} for no reaction
     */
    BlockState getStateWhenMovedIntoFluid(BlockState movedState, FluidState fluid, Level level, BlockPos destPos);

    /**
     * Called once the block has landed, for block entity data that can't live in the state.
     */
    default void onMovedIntoFluid(Level level, BlockPos destPos, BlockState placedState, FluidState fluid) {
    }
}
