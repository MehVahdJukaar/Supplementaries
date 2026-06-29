package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import vectorwing.farmersdelight.common.block.TomatoBlock;

import java.util.Map;

/**
 * A tomato crop that visually connects like our rope. Created when a vine climbs onto our rope.
 * Extends the FD crop (so it keeps growing/harvesting) and implements {@link IRopeConnection} for
 * the rope visuals/connections. The inherited {@code ROPELOGGED} property is left unused (false).
 */
public class TomatoRopeBlock extends TomatoBlock implements IRopeConnection {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty KNOT = ModBlockProperties.KNOT;

    // map horizontal directions to properties
    private static final Map<Direction, BooleanProperty> HMAP = Map.of(
            Direction.NORTH, NORTH,
            Direction.EAST, EAST,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST
    );

    public TomatoRopeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(VINE_AGE, 0).setValue(ROPELOGGED, false).setValue(KNOT, false)
                .setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // first the tomato properties
        super.createBlockStateDefinition(builder);
        // then the rope connection properties
        builder.add(NORTH, SOUTH, EAST, WEST, KNOT);
    }

    @Override
    public boolean canSideAcceptConnection(BlockState state, Direction direction) {
        // allow connections from any side (IRopeConnection helpers filter)
        return true;
    }

    @Override
    public boolean hasConnection(Direction dir, BlockState state) {
        BooleanProperty p = HMAP.get(dir);
        return p != null && state.getValue(p);
    }

    @Override
    public BlockState setConnection(Direction dir, BlockState state, boolean value) {
        BooleanProperty p = HMAP.get(dir);
        return p != null ? state.setValue(p, value) : state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        // keep the tomato survival scheduling
        super.updateShape(state, facing, facingState, world, currentPos, facingPos);
        return updateConnection(state, facing, currentPos, world);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(this.defaultBlockState(), context.getClickedPos(), context.getLevel());
    }

    // FD places the climbing state raw (via getClimbingState), so resolve our connections/knot here -
    // this makes the block correct no matter who places it, not only on player placement.
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            level.setBlock(pos, withConnections(state, pos, level), Block.UPDATE_CLIENTS);
        }
    }
}
