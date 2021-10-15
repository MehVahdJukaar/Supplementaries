package vazkii.quark.api;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IRotationLockable {

    /**
     * @param half -1 if not set, 0 if bottom, 1 if top
     */
    public BlockState applyRotationLock(Level world, BlockPos pos, BlockState currState, Direction direction, int half);

}