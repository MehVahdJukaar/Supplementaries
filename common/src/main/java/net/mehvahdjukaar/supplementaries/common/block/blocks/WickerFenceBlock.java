package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
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

import java.util.Map;

// A bad copy of wall block just because we cant cahnge its shape
public class WickerFenceBlock extends WaterBlock {
    public static final BooleanProperty UP = BlockStateProperties.UP;
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
        this.registerDefaultState(this.defaultBlockState().setValue(UP, true)
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
        VoxelShape voxelShape = Block.box(f, 0.0, f, g, wallPostHeight, g);
        VoxelShape voxelShape2 = Block.box(h, wallMinY, 0.0, i, wallLowHeight, i);
        VoxelShape voxelShape3 = Block.box(h, wallMinY, h, i, wallLowHeight, 16.0);
        VoxelShape voxelShape4 = Block.box(0.0, wallMinY, h, i, wallLowHeight, i);
        VoxelShape voxelShape5 = Block.box(h, wallMinY, h, 16.0, wallLowHeight, i);
        VoxelShape voxelShape6 = Block.box(h, wallMinY, 0.0, i, wallTallHeight, i);
        VoxelShape voxelShape7 = Block.box(h, wallMinY, h, i, wallTallHeight, 16.0);
        VoxelShape voxelShape8 = Block.box(0.0, wallMinY, h, i, wallTallHeight, i);
        VoxelShape voxelShape9 = Block.box(h, wallMinY, h, 16.0, wallTallHeight, i);
        ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

        for (Boolean up : UP.getPossibleValues()) {
            for (WallSide east : EAST_WALL.getPossibleValues()) {
                for (WallSide north : NORTH_WALL.getPossibleValues()) {
                    for (WallSide west : WEST_WALL.getPossibleValues()) {
                        for (WallSide south : SOUTH_WALL.getPossibleValues()) {
                            VoxelShape voxelShape10 = Shapes.empty();
                            voxelShape10 = applyWallShape(voxelShape10, east, voxelShape5, voxelShape9);
                            voxelShape10 = applyWallShape(voxelShape10, west, voxelShape4, voxelShape8);
                            voxelShape10 = applyWallShape(voxelShape10, north, voxelShape2, voxelShape6);
                            voxelShape10 = applyWallShape(voxelShape10, south, voxelShape3, voxelShape7);
                            if (up) {
                                voxelShape10 = Shapes.or(voxelShape10, voxelShape);
                            }

                            BlockState blockState = this.defaultBlockState().setValue(UP, up).setValue(EAST_WALL, east)
                                    .setValue(WEST_WALL, west).setValue(NORTH_WALL, north).setValue(SOUTH_WALL, south);
                            builder.put(blockState.setValue(WATERLOGGED, false), voxelShape10);
                            builder.put(blockState.setValue(WATERLOGGED, true), voxelShape10);
                        }
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
        BlockState state = super.getStateForPlacement(context);
        return this.updateShape(levelReader, state, abovePos, aboveState, north, east, south, weast);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        state = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        if (direction == Direction.DOWN) {
            return state;
        } else {
            return direction == Direction.UP ? this.topUpdate(level, state, neighborPos, neighborState) : this.sideUpdate(level, currentPos, state, neighborPos, neighborState, direction);
        }
    }

    private static boolean isConnected(BlockState state, Property<WallSide> heightProperty) {
        return state.getValue(heightProperty) != WallSide.NONE;
    }

    private static boolean isCovered(VoxelShape firstShape, VoxelShape secondShape) {
        return !Shapes.joinIsNotEmpty(secondShape, firstShape, BooleanOp.ONLY_FIRST);
    }

    private BlockState topUpdate(LevelReader level, BlockState state, BlockPos pos, BlockState secondState) {
        boolean bl = isConnected(state, NORTH_WALL);
        boolean bl2 = isConnected(state, EAST_WALL);
        boolean bl3 = isConnected(state, SOUTH_WALL);
        boolean bl4 = isConnected(state, WEST_WALL);
        return this.updateShape(level, state, pos, secondState, bl, bl2, bl3, bl4);
    }

    private BlockState sideUpdate(LevelReader level, BlockPos firstPos, BlockState firstState, BlockPos secondPos, BlockState secondState, Direction dir) {
        Direction direction = dir.getOpposite();
        boolean bl = dir == Direction.NORTH ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, direction), direction) : isConnected(firstState, NORTH_WALL);
        boolean bl2 = dir == Direction.EAST ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, direction), direction) : isConnected(firstState, EAST_WALL);
        boolean bl3 = dir == Direction.SOUTH ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, direction), direction) : isConnected(firstState, SOUTH_WALL);
        boolean bl4 = dir == Direction.WEST ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, direction), direction) : isConnected(firstState, WEST_WALL);
        BlockPos blockPos = firstPos.above();
        BlockState blockState = level.getBlockState(blockPos);
        return this.updateShape(level, firstState, blockPos, blockState, bl, bl2, bl3, bl4);
    }

    private BlockState updateShape(LevelReader level, BlockState state, BlockPos pos, BlockState neighbour, boolean northConnection, boolean eastConnection, boolean southConnection, boolean westConnection) {
        VoxelShape voxelShape = neighbour.getCollisionShape(level, pos).getFaceShape(Direction.DOWN);
        BlockState blockState = this.updateSides(state, northConnection, eastConnection, southConnection, westConnection, voxelShape);
        return blockState.setValue(UP, this.shouldRaisePost(blockState, neighbour, voxelShape));
    }

    private boolean shouldRaisePost(BlockState state, BlockState neighbour, VoxelShape shape) {
        boolean bl = neighbour.getBlock() instanceof WallBlock && neighbour.getValue(UP);
        if (bl) {
            return true;
        } else {
            WallSide wallSide = state.getValue(NORTH_WALL);
            WallSide wallSide2 = state.getValue(SOUTH_WALL);
            WallSide wallSide3 = state.getValue(EAST_WALL);
            WallSide wallSide4 = state.getValue(WEST_WALL);
            boolean bl2 = wallSide2 == WallSide.NONE;
            boolean bl3 = wallSide4 == WallSide.NONE;
            boolean bl4 = wallSide3 == WallSide.NONE;
            boolean bl5 = wallSide == WallSide.NONE;
            boolean bl6 = bl5 && bl2 && bl3 && bl4 || bl5 != bl2 || bl3 != bl4;
            if (bl6) {
                return true;
            } else {
                boolean bl7 = wallSide == WallSide.TALL && wallSide2 == WallSide.TALL || wallSide3 == WallSide.TALL && wallSide4 == WallSide.TALL;
                if (bl7) {
                    return false;
                } else {
                    return neighbour.is(BlockTags.WALL_POST_OVERRIDE) || isCovered(shape, POST_TEST);
                }
            }
        }
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
        builder.add(UP, NORTH_WALL, EAST_WALL, WEST_WALL, SOUTH_WALL);
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

