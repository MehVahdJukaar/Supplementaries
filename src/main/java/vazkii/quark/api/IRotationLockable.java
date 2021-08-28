package vazkii.quark.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRotationLockable {

    /**
     * @param half -1 if not set, 0 if bottom, 1 if top
     */
    public BlockState applyRotationLock(World world, BlockPos pos, BlockState currState, Direction direction, int half);

}