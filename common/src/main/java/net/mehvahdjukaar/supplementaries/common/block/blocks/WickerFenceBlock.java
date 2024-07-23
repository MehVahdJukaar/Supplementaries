package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// A bad copy of wall block just because we cant cahnge its shape
public class WickerFenceBlock extends WaterBlock {
    public static final EnumProperty<WallSide> EAST_WALL = BlockStateProperties.EAST_WALL;
    public static final EnumProperty<WallSide> NORTH_WALL = BlockStateProperties.NORTH_WALL;
    public static final EnumProperty<WallSide> SOUTH_WALL = BlockStateProperties.SOUTH_WALL;
    public static final EnumProperty<WallSide> WEST_WALL = BlockStateProperties.WEST_WALL;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final Map<BlockState, VoxelShape> shapeByIndex;
    private final Map<BlockState, VoxelShape> collisionShapeByIndex;
    private static final VoxelShape POST_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
    private static final VoxelShape NORTH_TEST = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 9.0);
    private static final VoxelShape SOUTH_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 16.0);
    private static final VoxelShape WEST_TEST = Block.box(0.0, 0.0, 7.0, 9.0, 16.0, 9.0);
    private static final VoxelShape EAST_TEST = Block.box(7.0, 0.0, 7.0, 16.0, 16.0, 9.0);


    public WickerFenceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(NORTH_WALL, WallSide.NONE).setValue(EAST_WALL, WallSide.NONE)
                .setValue(SOUTH_WALL, WallSide.NONE).setValue(WEST_WALL, WallSide.NONE));
        this.shapeByIndex = this.makeShapes(1, 1, 16.0F, 0.0F, 14, 16.0F);
        this.collisionShapeByIndex = this.makeShapes(1, 1, 24.0F, 0.0F, 24.0F, 24.0F);
    }

    private static VoxelShape applyWallShape(VoxelShape baseShape, WallSide height, VoxelShape lowShape, VoxelShape tallShape) {
        if (height == WallSide.TALL) {
            return Shapes.or(baseShape, tallShape);
        } else {
            return height == WallSide.LOW ? Shapes.or(baseShape, lowShape) : baseShape;
        }
    }

    private Map<BlockState, VoxelShape> makeShapes(float width, float depth, float wallPostHeight, float wallMinY, float wallLowHeight, float wallTallHeight) {
        float f = 8.0F - width;
        float g = 8.0F + width;
        float h = 8.0F - depth;
        float i = 8.0F + depth;
        VoxelShape postShape = Block.box(f, 0.0, f, g, wallPostHeight, g);
        VoxelShape northShape = Block.box(h, wallMinY, 0.0, i, wallLowHeight, i);
        VoxelShape southShape = Block.box(h, wallMinY, h, i, wallLowHeight, 16.0);
        VoxelShape westShape = Block.box(0.0, wallMinY, h, i, wallLowHeight, i);
        VoxelShape eastShape = Block.box(h, wallMinY, h, 16.0, wallLowHeight, i);
        VoxelShape northShapeTall = Block.box(h, wallMinY, 0.0, i, wallTallHeight, i);
        VoxelShape southShapeTall = Block.box(h, wallMinY, h, i, wallTallHeight, 16.0);
        VoxelShape weastShapeTall = Block.box(0.0, wallMinY, h, i, wallTallHeight, i);
        VoxelShape eastShapeTall = Block.box(h, wallMinY, h, 16.0, wallTallHeight, i);
        ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

        for (WallSide east : EAST_WALL.getPossibleValues()) {
            for (WallSide north : NORTH_WALL.getPossibleValues()) {
                for (WallSide west : WEST_WALL.getPossibleValues()) {
                    for (WallSide south : SOUTH_WALL.getPossibleValues()) {
                        VoxelShape conbinedShape = Shapes.empty();
                        conbinedShape = applyWallShape(conbinedShape, east, eastShape, eastShapeTall);
                        conbinedShape = applyWallShape(conbinedShape, west, westShape, weastShapeTall);
                        conbinedShape = applyWallShape(conbinedShape, north, northShape, northShapeTall);
                        conbinedShape = applyWallShape(conbinedShape, south, southShape, southShapeTall);

                        conbinedShape = Shapes.or(conbinedShape, postShape);

                        BlockState blockState = this.defaultBlockState().setValue(EAST_WALL, east)
                                .setValue(WEST_WALL, west).setValue(NORTH_WALL, north).setValue(SOUTH_WALL, south);
                        builder.put(blockState.setValue(WATERLOGGED, false), conbinedShape);
                        builder.put(blockState.setValue(WATERLOGGED, true), conbinedShape);
                    }
                }
            }
        }

        return builder.build();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapeByIndex.get(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.collisionShapeByIndex.get(state);
    }

    private boolean connectsTo(BlockState state, boolean sideSolid, Direction direction) {
        Block block = state.getBlock();
        boolean fenceGate = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(state, direction);
        return state.is(this) || !isExceptionForConnection(state) && sideSolid || block instanceof IronBarsBlock || fenceGate;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader levelReader = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState state = super.getStateForPlacement(context);
        return allUpdate(state, blockPos, levelReader);
    }

    //update all. we need this
    private @NotNull BlockState allUpdate(BlockState state, BlockPos blockPos, LevelReader levelReader) {
        BlockPos northPos = blockPos.north();
        BlockPos eastPos = blockPos.east();
        BlockPos southPos = blockPos.south();
        BlockPos westPos = blockPos.west();
        BlockPos abovePos = blockPos.above();
        BlockState northState = levelReader.getBlockState(northPos);
        BlockState eastState = levelReader.getBlockState(eastPos);
        BlockState southState = levelReader.getBlockState(southPos);
        BlockState westState = levelReader.getBlockState(westPos);
        BlockState aboveState = levelReader.getBlockState(abovePos);

        boolean north = this.connectsTo(northState, northState.isFaceSturdy(levelReader, northPos, Direction.SOUTH), Direction.SOUTH);
        boolean east = this.connectsTo(eastState, eastState.isFaceSturdy(levelReader, eastPos, Direction.WEST), Direction.WEST);
        boolean south = this.connectsTo(southState, southState.isFaceSturdy(levelReader, southPos, Direction.NORTH), Direction.NORTH);
        boolean weast = this.connectsTo(westState, westState.isFaceSturdy(levelReader, westPos, Direction.EAST), Direction.EAST);

        if (!east && !weast && !north && !south) {
            BlockState belowState = levelReader.getBlockState(blockPos.below());
            if (belowState.is(this)) {
                east = belowState.getValue(EAST_WALL) != WallSide.NONE;
                weast = belowState.getValue(WEST_WALL) != WallSide.NONE;
                north = belowState.getValue(NORTH_WALL) != WallSide.NONE;
                south = belowState.getValue(SOUTH_WALL) != WallSide.NONE;
            } else {
                east = true;
                weast = true;
                north = true;
                south = true;
            }
        }

        return this.getCorrectShape(levelReader, state, abovePos, aboveState, north, east, south, weast);
    }


    private BlockState getCorrectShape(LevelReader level, BlockState state, BlockPos pos, BlockState upState, boolean northConnection, boolean eastConnection, boolean southConnection, boolean westConnection) {
        VoxelShape voxelShape = upState.getCollisionShape(level, pos).getFaceShape(Direction.DOWN);
        return this.updateSides(state, northConnection, eastConnection, southConnection, westConnection, voxelShape);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        state = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        return switch (direction) {
            case DOWN -> this.allUpdate(state, currentPos, level);
            case UP -> this.topUpdate(level, state, neighborPos, neighborState);
            default -> this.allUpdate(state, currentPos, level);
        };
    }

    private static boolean isConnected(BlockState state, Property<WallSide> heightProperty) {
        return state.getValue(heightProperty) != WallSide.NONE;
    }

    private static boolean isCovered(VoxelShape firstShape, VoxelShape secondShape) {
        return !Shapes.joinIsNotEmpty(secondShape, firstShape, BooleanOp.ONLY_FIRST);
    }

    private BlockState topUpdate(LevelReader level, BlockState state, BlockPos pos, BlockState secondState) {
        boolean north = isConnected(state, NORTH_WALL);
        boolean east = isConnected(state, EAST_WALL);
        boolean south = isConnected(state, SOUTH_WALL);
        boolean west = isConnected(state, WEST_WALL);
        return this.getCorrectShape(level, state, pos, secondState, north, east, south, west);
    }

    private BlockState updateSides(BlockState state, boolean northConnection, boolean eastConnection, boolean southConnection, boolean westConnection, VoxelShape wallShape) {
        return state.setValue(NORTH_WALL, this.makeWallState(northConnection, wallShape, NORTH_TEST))
                .setValue(EAST_WALL, this.makeWallState(eastConnection, wallShape, EAST_TEST))
                .setValue(SOUTH_WALL, this.makeWallState(southConnection, wallShape, SOUTH_TEST))
                .setValue(WEST_WALL, this.makeWallState(westConnection, wallShape, WEST_TEST));
    }

    private WallSide makeWallState(boolean allowConnection, VoxelShape shape, VoxelShape neighbourShape) {
        if (allowConnection) {
            return isCovered(shape, neighbourShape) ? WallSide.TALL : WallSide.LOW;
        } else {
            return WallSide.NONE;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH_WALL, EAST_WALL, WEST_WALL, SOUTH_WALL);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 ->
                    state.setValue(NORTH_WALL, state.getValue(SOUTH_WALL)).setValue(EAST_WALL, state.getValue(WEST_WALL))
                            .setValue(SOUTH_WALL, state.getValue(NORTH_WALL)).setValue(WEST_WALL, state.getValue(EAST_WALL));
            case COUNTERCLOCKWISE_90 ->
                    state.setValue(NORTH_WALL, state.getValue(EAST_WALL)).setValue(EAST_WALL, state.getValue(SOUTH_WALL))
                            .setValue(SOUTH_WALL, state.getValue(WEST_WALL)).setValue(WEST_WALL, state.getValue(NORTH_WALL));
            case CLOCKWISE_90 ->
                    state.setValue(NORTH_WALL, state.getValue(WEST_WALL)).setValue(EAST_WALL, state.getValue(NORTH_WALL))
                            .setValue(SOUTH_WALL, state.getValue(EAST_WALL)).setValue(WEST_WALL, state.getValue(SOUTH_WALL));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT ->
                    state.setValue(NORTH_WALL, state.getValue(SOUTH_WALL)).setValue(SOUTH_WALL, state.getValue(NORTH_WALL));
            case FRONT_BACK ->
                    state.setValue(EAST_WALL, state.getValue(WEST_WALL)).setValue(WEST_WALL, state.getValue(EAST_WALL));
            default -> super.mirror(state, mirror);
        };
    }

}

