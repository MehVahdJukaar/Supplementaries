package net.mehvahdjukaar.supplementaries.blocks;


import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
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


public class CrankBlock extends Block implements IWaterLoggable{
    protected static final VoxelShape SHAPE_DOWN = VoxelShapes.create(0.125D, 0.6875D, 0.875D, 0.875D, 1D, 0.125D);
    protected static final VoxelShape SHAPE_UP = VoxelShapes.create(0.125D, 0.3125D, 0.125D, 0.875D, 0D, 0.875D);
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.create(0.125D, 0.125D, 0.6875D, 0.875D, 0.875D, 1D);
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(0.875D, 0.125D, 0.3125D, 0.125D, 0.875D, 0D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.create(0.3125D, 0.125D, 0.125D, 0D, 0.875D, 0.875D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.create(0.6875D, 0.125D, 0.875D, 1D, 0.875D, 0.125D);

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public CrankBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED,false).with(POWER, 0).with(FACING, Direction.NORTH));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return facing.getOpposite() == stateIn.get(FACING) && !stateIn.isValidPosition(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState()
                : stateIn;
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return hasEnoughSolidSide(worldIn, blockpos, direction);
        } else {
            return blockstate.isSolidSide(worldIn, blockpos, direction);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            Direction direction = state.get(FACING).getOpposite();
            // Direction direction1 = getFacing(state).getOpposite();
            double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getXOffset() + 0.2D * (double) direction.getXOffset();
            double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getYOffset() + 0.2D * (double) direction.getYOffset();
            double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getZOffset() + 0.2D * (double) direction.getZOffset();
            worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0, 0);
        } else {
            this.activate(state, worldIn, pos);
            float f = 0.4f;
            worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
        }
        return ActionResultType.SUCCESS;
    }

    public void activate(BlockState state, World world, BlockPos pos) {
        //func_235896_a_ == cycle
        state = state.func_235896_a_(POWER);
        world.setBlockState(pos, state, 3);
        this.updateNeighbors(state, world, pos);
    }

    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWER);
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(FACING) == side ? blockState.get(POWER) : 0;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.notifyNeighborsOfStateChange(pos, this);
        world.notifyNeighborsOfStateChange(pos.offset(state.get(FACING).getOpposite()), this);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            if (state.get(POWER) != 0) {
                this.updateNeighbors(state, worldIn, pos);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, WATERLOGGED);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(WATERLOGGED, flag).with(FACING, context.getFace());
    }
}
