package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class CrankBlock extends WaterBlock {
    protected static final VoxelShape SHAPE_DOWN = VoxelShapes.box(0.125D, 0.6875D, 0.875D, 0.875D, 1D, 0.125D);
    protected static final VoxelShape SHAPE_UP = VoxelShapes.box(0.125D, 0.3125D, 0.125D, 0.875D, 0D, 0.875D);
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.box(0.125D, 0.125D, 0.6875D, 0.875D, 0.875D, 1D);
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.box(0.875D, 0.125D, 0.3125D, 0.125D, 0.875D, 0D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.box(0.3125D, 0.125D, 0.125D, 0D, 0.875D, 0.875D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.box(0.6875D, 0.125D, 0.875D, 1D, 0.875D, 0.125D);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public CrankBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(POWER, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing.getOpposite() == stateIn.getValue(FACING) && !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : stateIn;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return canSupportCenter(worldIn, blockpos, direction);
        } else {
            return blockstate.isFaceSturdy(worldIn, blockpos, direction);
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        if (worldIn.isClientSide) {
            Direction direction = state.getValue(FACING).getOpposite();
            // Direction direction1 = getFacing(state).getOpposite();
            double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getStepX() + 0.2D * (double) direction.getStepX();
            double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getStepY() + 0.2D * (double) direction.getStepY();
            double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getStepZ() + 0.2D * (double) direction.getStepZ();
            worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0, 0);
            return ActionResultType.SUCCESS;
        } else {
            boolean ccw = player.isShiftKeyDown();
            this.activate(state, worldIn, pos, ccw);
            float f = 0.4f;
            worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);


            Direction dir = state.getValue(FACING).getOpposite();
            if(dir.getAxis()!= Direction.Axis.Y) {
                BlockPos behind = pos.relative(dir);
                BlockState backState = worldIn.getBlockState(behind);
                if (backState.getBlock() instanceof PulleyBlock && dir.getAxis() == backState.getValue(PulleyBlock.AXIS)) {
                    ((PulleyBlock) backState.getBlock()).axisRotate(backState, behind, worldIn, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90);
                }
            }
            return ActionResultType.CONSUME;
        }
    }

    public void activate(BlockState state, World world, BlockPos pos, boolean ccw) {
        //cycle == cycle
        state = state.setValue(POWER, (16+state.getValue(POWER)+(ccw?-1:1))%16);
        world.setBlock(pos, state, 3);
        this.updateNeighbors(state, world, pos);
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWER);
    }

    @Override
    public int getDirectSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(FACING) == side ? blockState.getValue(POWER) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        world.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            if (state.getValue(POWER) != 0) {
                this.updateNeighbors(state, worldIn, pos);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }


    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.getValue(POWER)>0 && rand.nextFloat() < 0.25F) {
            Direction direction = stateIn.getValue(FACING).getOpposite();
            // Direction direction1 = getFacing(state).getOpposite();
            double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getStepX() + 0.2D * (double) direction.getStepX();
            double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getStepY() + 0.2D * (double) direction.getStepY();
            double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getStepZ() + 0.2D * (double) direction.getStepZ();
            worldIn.addParticle(new RedstoneParticleData(1.0F, 0.0F, 0.0F, 0.5f), d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.OPEN;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING)) {
            case SOUTH :
            default :
                return SHAPE_SOUTH;
            case NORTH :
                return SHAPE_NORTH;
            case WEST :
                return SHAPE_WEST;
            case EAST :
                return SHAPE_EAST;
            case UP :
                return SHAPE_UP;
            case DOWN :
                return SHAPE_DOWN;
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, WATERLOGGED);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState blockstate = this.defaultBlockState();
        IWorldReader iworldreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction[] adirection = context.getNearestLookingDirections();

        for(Direction direction : adirection) {

            Direction direction1 = direction.getOpposite();
            blockstate = blockstate.setValue(FACING, direction1);
            if (blockstate.canSurvive(iworldreader, blockpos)) {
                return blockstate.setValue(WATERLOGGED, flag);
            }

        }
        return null;
    }
}
