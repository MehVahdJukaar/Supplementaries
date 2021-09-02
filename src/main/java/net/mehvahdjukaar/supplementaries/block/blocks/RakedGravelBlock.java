package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.RakeDirection;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(RAKE_DIRECTION);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = super.defaultBlockState();
        BlockPos pos = context.getClickedPos();
        World world = context.getLevel();
        if(!blockstate.canSurvive(world, pos)){
            return Block.pushEntitiesUp(blockstate, Blocks.GRAVEL.defaultBlockState(), world, pos);
        }
        Direction front = context.getHorizontalDirection();
        return getConnectedState(blockstate,world,pos,front);

    }

    private static boolean canConnect(BlockState state, Direction dir){
        if(state.getBlock() == ModRegistry.RAKED_GRAVEL.get()){
            return state.getValue(RAKE_DIRECTION).getDirections().contains(dir.getOpposite());
        }
        return false;
    }

    public static BlockState getConnectedState(BlockState blockstate, World world, BlockPos pos, Direction front){
        List<Direction> directionList = new ArrayList<>();

        Direction back = front.getOpposite();
        if(canConnect(world.getBlockState(pos.relative(back)),back)){
            directionList.add(back);
        }
        else{
            directionList.add(front);
        }

        Direction side = front.getClockWise();

        for(int i = 0; i<2; i++){
            BlockState state = world.getBlockState(pos.relative(side));
            if(canConnect(state,side)){
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
        switch(rotation) {
            case CLOCKWISE_180:
                switch(shape) {
                    case SOUTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                    case SOUTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                    case NORTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                    case NORTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                    default:
                        return state;
                }
            case COUNTERCLOCKWISE_90:
                switch(shape) {
                    case SOUTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                    case SOUTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                    case NORTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                    case NORTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                    case NORTH_SOUTH:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.EAST_WEST);
                    case EAST_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_SOUTH);
                }
            case CLOCKWISE_90:
                switch(shape) {
                    case SOUTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                    case SOUTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                    case NORTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                    case NORTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                    case NORTH_SOUTH:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.EAST_WEST);
                    case EAST_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_SOUTH);
                }
            default:
                return state;
        }
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        RakeDirection shape = state.getValue(RAKE_DIRECTION);
        switch(mirror) {
            case LEFT_RIGHT:
                switch(shape) {
                    case SOUTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                    case SOUTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                    case NORTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                    case NORTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                    default:
                        return super.mirror(state, mirror);
                }
            case FRONT_BACK:
                switch(shape) {
                    default:
                        break;
                    case SOUTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_WEST);
                    case SOUTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.SOUTH_EAST);
                    case NORTH_WEST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_EAST);
                    case NORTH_EAST:
                        return state.setValue(RAKE_DIRECTION, RakeDirection.NORTH_WEST);
                }
        }
        return super.mirror(state, mirror);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState fromState, IWorld world, BlockPos pos, BlockPos fromPos) {
        if (direction == Direction.UP && !state.canSurvive(world, pos)) {
            world.getBlockTicks().scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, fromState, world, pos, fromPos);
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if(!state.canSurvive(world, pos)) turnToGravel(state, world, pos);
        super.tick(state,world,pos,random);
    }

    public static void turnToGravel(BlockState state, World world, BlockPos pos) {
        world.setBlockAndUpdate(pos, pushEntitiesUp(state, Blocks.GRAVEL.defaultBlockState(), world, pos));
    }

    @Override
    public boolean canSurvive(BlockState p_196260_1_, IWorldReader p_196260_2_, BlockPos p_196260_3_) {
        BlockState blockstate = p_196260_2_.getBlockState(p_196260_3_.above());
        return !blockstate.getMaterial().isSolid() || blockstate.getBlock() instanceof FenceGateBlock;
    }

}