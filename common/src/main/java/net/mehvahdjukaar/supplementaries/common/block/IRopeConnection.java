package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.amendments.mixins.LanternBlockPlacementMixin;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public interface IRopeConnection {

    default boolean shouldConnectToFace(BlockState thisState, BlockState facingState, BlockPos facingPos, Direction dir, LevelReader world) {
        if (!this.canSideAcceptConnection(thisState, dir)) return false;

        switch (dir) {
            case UP -> {
                return isSupportingCeiling(facingState, facingPos, world);
            }
            case DOWN -> {
                return isSupportingCeiling(facingPos.above(2), world) || canConnectDown(facingState);
            }
            default -> {
                if (CommonConfigs.Functional.ROPE_UNRESTRICTED.get() && facingState.isFaceSturdy(world, facingPos, dir.getOpposite())) {
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


    static boolean isSupportingCeiling(BlockPos pos, LevelReader world) {
        return isSupportingCeiling(world.getBlockState(pos), pos, world);
    }

    static boolean canConnectDown(BlockState downState) {
        Block b = downState.getBlock();
        if (b instanceof IRopeConnection ropeConnection) {
            return ropeConnection.canSideAcceptConnection(downState, Direction.UP);
        }
        return (downState.is(ModTags.ROPE_HANG_TAG)
                || (downState.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE) && downState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING)
                || (b instanceof ChainBlock && downState.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y)
                || (downState.hasProperty(BlockStateProperties.HANGING) && downState.getValue(BlockStateProperties.HANGING)));
    }

    static boolean isSupportingCeiling(BlockState upState, BlockPos pos, LevelReader world) {
        if (upState.getBlock() instanceof IRopeConnection ropeConnection) {
            return ropeConnection.canSideAcceptConnection(upState, Direction.DOWN);
        }
        return (Block.canSupportCenter(world, pos, Direction.DOWN) && upState.isSolid()) || upState.is(ModTags.ROPE_SUPPORT_TAG);
    }
}
