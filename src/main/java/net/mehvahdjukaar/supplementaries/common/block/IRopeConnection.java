package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IRopeConnection {

    default boolean shouldConnectToFace(BlockState thisState, BlockState facingState, BlockPos facingPos, Direction dir, LevelReader world) {
        if (!this.canSideAcceptConnection(thisState, dir)) return false;

        switch (dir) {
            case UP -> {
                return RopeBlock.isSupportingCeiling(facingState, facingPos, world);
            }
            case DOWN -> {
                return RopeBlock.isSupportingCeiling(facingPos.above(2), world) || RopeBlock.canConnectDown(facingState);
            }
            default -> {
                if (ServerConfigs.block.ROPE_UNRESTRICTED.get() && facingState.isFaceSturdy(world, facingPos, dir.getOpposite())) {
                    return true;
                }
                if (facingState.getBlock() instanceof IRopeConnection otherRope) {
                    return otherRope.canSideAcceptConnection(facingState, dir.getOpposite());
                }
                return false;
            }
        }
    }

    boolean canSideAcceptConnection(BlockState state, Direction direction);
}
