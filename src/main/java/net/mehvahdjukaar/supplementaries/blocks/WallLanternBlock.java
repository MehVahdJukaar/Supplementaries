package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;


public class WallLanternBlock extends Block implements  IWaterLoggable{
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(0.6875D, 0.125D, 0.625D, 0.3125D, 1D, 0D);
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.create(0.3125D, 0.125D, 0.375D, 0.6875D, 1D, 1D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.create(0.375D, 0.125D, 0.6875D, 1D, 1D, 0.3125D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.create(0.625D, 0.125D, 0.3125D, 0D, 1D, 0.6875D);

    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final IntegerProperty LIGHT_LEVEL = CommonUtil.LIGHT_LEVEL_0_15;
    public static final IntegerProperty EXTENSION = CommonUtil.EXTENSION;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public WallLanternBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(LIGHT_LEVEL, 15).with(WATERLOGGED,false));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        /*
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            return ((WallLanternBlockTile) te).lanternBlock.getLightValue();
        }*/
        //may cause lag
        return state.get(LIGHT_LEVEL);
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            return new ItemStack(((WallLanternBlockTile) te).lanternBlock.getBlock());
        }
        return new ItemStack(Blocks.LANTERN, 1);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        Block block =  blockstate.getBlock();
        return (blockstate.isSolidSide(worldIn, blockpos, direction)||block instanceof FenceBlock || block instanceof SignPostBlock ||block instanceof WallBlock);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return facing == stateIn.get(FACING).getOpposite() && !stateIn.isValidPosition(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState()
                : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof WallLanternBlockTile) {
            ((WallLanternBlockTile) tileentity).counter = 0;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            case UP :
            case DOWN :
            case SOUTH :
            default :
                return SHAPE_SOUTH;
            case NORTH :
                return SHAPE_NORTH;
            case WEST :
                return SHAPE_WEST;
            case EAST :
                return SHAPE_EAST;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIGHT_LEVEL, EXTENSION, WATERLOGGED);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN)
            return this.getDefaultState().with(FACING, Direction.NORTH);
        BlockPos blockpos = context.getPos();
        IBlockReader world = context.getWorld();
        Block block = world.getBlockState(blockpos.offset(context.getFace().getOpposite())).getBlock();

        boolean flag = world.getFluidState(blockpos).getFluid() == Fluids.WATER;;
        boolean ext = (block instanceof FenceBlock || block instanceof SignPostBlock ||block instanceof WallBlock);

        return this.getDefaultState().with(FACING, context.getFace()).with(EXTENSION, ext? 1:0).with(WATERLOGGED,flag);
    }

/*
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            spawnDrops(((WallLanternBlockTile) te).lanternBlock, worldIn, pos);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }*/

    //can't use getDrops cause it doesn't have pos.
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof WallLanternBlockTile) {
                spawnDrops(((WallLanternBlockTile) te).lanternBlock, worldIn, pos);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new WallLanternBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}