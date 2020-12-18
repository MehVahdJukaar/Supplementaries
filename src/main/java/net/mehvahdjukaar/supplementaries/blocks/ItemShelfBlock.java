package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.blocks.tiles.ItemShelfBlockTile;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;


public class ItemShelfBlock extends Block implements IWaterLoggable {
    //protected static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(3.0D, 2.0D, 13.0D, 13.0D, 4.0D, 16.0D);
    //protected static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(3.0D, 2.0D, 0.0D, 13.0D, 4.0D, 3.0D);
    //protected static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(13.0D, 2.0D, 3.0D, 16.0D, 4.0D, 13.0D);
    //protected static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(0.0D, 2.0D, 3.0D, 3.0D, 4.0D, 13.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(0D, 1.0D, 13.0D, 16.0D, 4.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(0D, 1.0D, 0.0D, 16.0D, 4.0D, 3.0D);
    protected static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(13.0D, 1.0D, 0D, 16.0D, 4.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(0.0D, 1.0D, 0D, 3.0D, 4.0D, 16.0D);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public ItemShelfBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED,false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED,FACING);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite())).getMaterial().isSolid();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        boolean flag = world.getFluidState(pos).getFluid() == Fluids.WATER;
        if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN)
            return this.getDefaultState().with(FACING, Direction.NORTH).with(WATERLOGGED,flag);
        return this.getDefaultState().with(FACING, context.getFace()).with(WATERLOGGED,flag);
    }

    //called when a neighbor is placed
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return facing == stateIn.get(FACING).getOpposite() && !stateIn.isValidPosition(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState()
                : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if(target.getHitVec().getY() >= pos.getY()+0.25) {
            if (te instanceof ItemShelfBlockTile) {
                ItemStack i = ((ItemShelfBlockTile) te).getStackInSlot(0);
                if (!i.isEmpty()) return i;
            }
        }
        return new ItemStack(this, 1);

    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof ItemShelfBlockTile) {
            ItemShelfBlockTile te = (ItemShelfBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            boolean flag1 = (te.isEmpty() && !itemstack.isEmpty() && (te.isItemValidForSlot(0, itemstack)));
            boolean flag2 = (itemstack.isEmpty() && !te.isEmpty());
            if (flag1) {
                ItemStack it = itemstack.copy();
                it.setCount(1);
                NonNullList<ItemStack> stacks = NonNullList.withSize(1, it);
                te.setItems(stacks);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                if(!worldIn.isRemote()){
                    worldIn.playSound(null, pos,SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM,SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.10F + 0.95F);
                    te.markDirty();
                }
                return ActionResultType.SUCCESS;
            }
            else if (flag2) {
                ItemStack it = te.removeStackFromSlot(0);
                player.setHeldItem(handIn, it);
                if(!worldIn.isRemote()){
                    te.markDirty();
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)){
            default:
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
        }
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ItemShelfBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof ItemShelfBlockTile) {
                InventoryHelper.dropInventoryItems(world, pos, (ItemShelfBlockTile) tileentity);
                world.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof ItemShelfBlockTile)
            return Container.calcRedstoneFromInventory((ItemShelfBlockTile) tileentity);
        else
            return 0;
    }
}