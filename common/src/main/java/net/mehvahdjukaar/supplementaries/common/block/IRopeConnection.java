package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public interface IRopeConnection {

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

    // ==== Shared connection-state machinery (used by AbstractRopeBlock and the rope-tomato) ====

    boolean hasConnection(Direction dir, BlockState state);

    BlockState setConnection(Direction dir, BlockState state, boolean value);

    default boolean hasMiddleKnot(BlockState state) {
        boolean up = hasConnection(Direction.UP, state);
        boolean down = hasConnection(Direction.DOWN, state);
        boolean north = hasConnection(Direction.NORTH, state);
        boolean east = hasConnection(Direction.EAST, state);
        boolean south = hasConnection(Direction.SOUTH, state);
        boolean west = hasConnection(Direction.WEST, state);
        //not inverse
        return !((up && down && !north && !south && !east && !west)
                || (!up && !down && north && south && !east && !west)
                || (!up && !down && !north && !south && east && west));
    }

    default boolean shouldConnectToDir(BlockState thisState, BlockPos currentPos, LevelReader world, Direction dir) {
        if (dir.getAxis().isHorizontal() && !CommonConfigs.Functional.ROPE_HORIZONTAL.get()) return false;
        BlockPos facingPos = currentPos.relative(dir);
        return this.shouldConnectToFace(thisState, world.getBlockState(facingPos), facingPos, dir, world);
    }

    // computes every connection plus the knot, for placement
    default BlockState withConnections(BlockState state, BlockPos pos, LevelReader world) {
        for (Direction dir : Direction.values()) {
            state = setConnection(dir, state, shouldConnectToDir(state, pos, world, dir));
        }
        return state.setValue(ModBlockProperties.KNOT, hasMiddleKnot(state));
    }

    // recomputes the connection towards the changed face (plus the knot), for updateShape
    default BlockState updateConnection(BlockState state, Direction facing, BlockPos currentPos, LevelReader world) {
        if (facing == Direction.UP) {
            state = setConnection(Direction.DOWN, state, shouldConnectToDir(state, currentPos, world, Direction.DOWN));
        }
        state = setConnection(facing, state, shouldConnectToDir(state, currentPos, world, facing));
        return state.setValue(ModBlockProperties.KNOT, hasMiddleKnot(state));
    }
}
