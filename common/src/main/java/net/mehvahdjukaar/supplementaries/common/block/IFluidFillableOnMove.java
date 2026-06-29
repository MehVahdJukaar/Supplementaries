package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Implement on a block that should react when it is moved (pushed by a piston or dragged by a
 * pulley/rope) into a position that contained a fluid source — the way a cauldron fills with the
 * fluid it's shoved into.
 * <p>
 * The reaction happens in up to two phases:
 * <ul>
 *   <li>{@link #getStateWhenMovedIntoFluid} runs while the block is being relocated and decides the
 *   {@link BlockState} that lands. For animated moves (pistons, continuous pulleys) this is the
 *   state the moving-block animation carries, so put any visible change here.</li>
 *   <li>{@link #onMovedIntoFluid} runs once the block has physically landed at its destination, for
 *   populating block-entity data that can't live in the {@link BlockState} (e.g. a fluid tank).</li>
 * </ul>
 * Both phases only fire when the {@code moved_cauldron_filling} tweak is enabled. Vanilla and
 * Amendments cauldrons are handled built-in and do not need this interface; it exists so other
 * mods' blocks can opt into the same behavior.
 */
public interface IFluidFillableOnMove {

    /**
     * @param movedState the state of this block as it is being moved
     * @param fluid      the fluid source that occupied the destination
     * @param level      the level
     * @param destPos    the position the block is landing at (still mid-animation for piston moves)
     * @return the state to place; return {@code movedState} unchanged for no reaction
     */
    BlockState getStateWhenMovedIntoFluid(BlockState movedState, FluidState fluid, Level level, BlockPos destPos);

    /**
     * Called after the state from {@link #getStateWhenMovedIntoFluid} has actually been placed at
     * {@code destPos}. Use it to set up block-entity data from the fluid. Default no-op.
     */
    default void onMovedIntoFluid(Level level, BlockPos destPos, BlockState placedState, FluidState fluid) {
    }
}
