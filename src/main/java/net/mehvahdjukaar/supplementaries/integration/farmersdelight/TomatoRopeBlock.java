package net.mehvahdjukaar.supplementaries.integration.farmersdelight;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class TomatoRopeBlock extends TomatoLoggedBlock implements IRopeConnection {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty KNOT = BlockProperties.KNOT;

    public TomatoRopeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ROPELOGGED, true).setValue(KNOT, false)
                .setValue(EAST, false).setValue(WEST, false).setValue(NORTH, false).setValue(SOUTH, false));
    }

    @Override
    public Block getInnerBlock() {
        return ModRegistry.ROPE.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, KNOT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
                                  BlockPos facingPos) {
        super.updateShape(state, facing, facingState, world, currentPos, facingPos);

        if (facing == Direction.DOWN && !world.isClientSide()) {
            FDCompatRegistry.tryTomatoLogging(facingState, world, facingPos, true);
        }

        if (facing.getAxis() == Direction.Axis.Y) {
            return state;
        }
        BlockState newState = state.setValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(facing), this.shouldConnectToFace(state, facingState, facingPos, facing, world));
        boolean hasKnot = newState.getValue(SOUTH) || newState.getValue(EAST) || newState.getValue(NORTH) || newState.getValue(WEST);
        newState = newState.setValue(KNOT, hasKnot);

        return newState;
    }

    @Override
    public boolean canSideAcceptConnection(BlockState state, Direction direction) {
        return true;
    }
}


