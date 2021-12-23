package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties.RakeDirection;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RakedGravelBlock extends GravelBlock {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public static final EnumProperty<RakeDirection> RAKE_DIRECTION = BlockProperties.RAKE_DIRECTION;

    public RakedGravelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(RAKE_DIRECTION, RakeDirection.NORTH_SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RAKE_DIRECTION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = super.defaultBlockState();
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        if (!blockstate.canSurvive(world, pos)) {
            return Block.pushEntitiesUp(blockstate, Blocks.GRAVEL.defaultBlockState(), world, pos);
        }
        Direction front = context.getHorizontalDirection();
        return getConnectedState(blockstate, world, pos, front);

    }

    private static boolean canConnect(BlockState state, Direction dir) {
        if (state.getBlock() == ModRegistry.RAKED_GRAVEL.get()) {
            return state.getValue(RAKE_DIRECTION).getDirections().contains(dir.getOpposite());
        }
        return false;
    }

    public static BlockState getConnectedState(BlockState blockstate, Level world, BlockPos pos, Direction front) {
        List<Direction> directionList = new ArrayList<>();

        Direction back = front.getOpposite();
        if (canConnect(world.getBlockState(pos.relative(back)), back)) {
            directionList.add(back);
        } else {
            directionList.add(front);
        }

        Direction side = front.getClockWise();

        for (int i = 0; i < 2; i++) {
            BlockState state = world.getBlockState(pos.relative(side));
            if (canConnect(state, side)) {
                directionList.add(side);
                break;
            }
            side = side.getOpposite();
        }

        return blockstate.setValue(RAKE_DIRECTION, RakeDirection.fromDirections(directionList));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        RakeDirection shape = state.getValue(RAKE_DIRECTION);
        return switch (rotation) {
            case CLOCKWISE_180 -> switch (shape) {
                case SOUTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                case SOUTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                case NORTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                case NORTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                default -> state;
            };
            case COUNTERCLOCKWISE_90 -> switch (shape) {
                case SOUTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                case SOUTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                case NORTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                case NORTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                case NORTH_SOUTH -> state.setValue(RAKE_DIRECTION, RakeDirection.EAST_WEST);
                case EAST_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_SOUTH);
            };
            case CLOCKWISE_90 -> switch (shape) {
                case SOUTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                case SOUTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                case NORTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                case NORTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                case NORTH_SOUTH -> state.setValue(RAKE_DIRECTION, RakeDirection.EAST_WEST);
                case EAST_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_SOUTH);
            };
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        RakeDirection shape = state.getValue(RAKE_DIRECTION);
        return switch (mirror) {
            case LEFT_RIGHT -> switch (shape) {
                case SOUTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                case SOUTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                case NORTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                case NORTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                default -> super.mirror(state, mirror);
            };
            case FRONT_BACK -> switch (shape) {
                default -> super.mirror(state, mirror);
                case SOUTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                case SOUTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                case NORTH_WEST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                case NORTH_EAST -> state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
            };
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType pathType) {
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState fromState, LevelAccessor world, BlockPos pos, BlockPos fromPos) {
        if (direction == Direction.UP && !state.canSurvive(world, pos)) {
            world.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, fromState, world, pos, fromPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        if (!state.canSurvive(world, pos)) turnToGravel(state, world, pos);
        super.tick(state, world, pos, random);
    }

    public static void turnToGravel(BlockState state, Level world, BlockPos pos) {
        world.setBlockAndUpdate(pos, pushEntitiesUp(state, Blocks.GRAVEL.defaultBlockState(), world, pos));
    }

    @Override
    public boolean canSurvive(BlockState p_196260_1_, LevelReader p_196260_2_, BlockPos p_196260_3_) {
        BlockState blockstate = p_196260_2_.getBlockState(p_196260_3_.above());
        return !blockstate.getMaterial().isSolid() || blockstate.getBlock() instanceof FenceGateBlock;
    }

}