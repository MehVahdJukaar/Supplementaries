package net.mehvahdjukaar.supplementaries.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;


public class PedestalBlock extends Block {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public PedestalBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(UP, false).with(DOWN, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(UP,DOWN);
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        return this.getDefaultState().with(UP, canConnect(world.getBlockState(pos.up()), pos, world, Direction.UP))
                .with(DOWN, canConnect(world.getBlockState(pos.down()), pos, world, Direction.DOWN));
    }


    public static boolean canConnect(BlockState state, BlockPos pos, World world, Direction dir){
        if(state.getBlock() instanceof  PedestalBlock) {
            if (dir == Direction.DOWN) {
                TileEntity te = world.getTileEntity(pos.down());
                if(te instanceof PedestalBlockTile) {
                    return ((PedestalBlockTile)te).isEmpty();
                }
            }
            else if (dir == Direction.UP) {
                TileEntity te = world.getTileEntity(pos);
                if(te instanceof PedestalBlockTile) {
                    return ((PedestalBlockTile)te).isEmpty();
                }
            }
        }
        return false;
    }

    //called when a neighbor is placed
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if(facing==Direction.UP){
            return stateIn.with(UP, canConnect(facingState, currentPos, (World)worldIn, facing));
        }
        else if(facing==Direction.DOWN){
            return stateIn.with(DOWN, canConnect(facingState, currentPos, (World)worldIn, facing));
        }
        return stateIn;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if(target.getHitVec().getY() > pos.getY()+1-0.1875) {
            if (te instanceof PedestalBlockTile) {
                ItemStack i = ((PedestalBlockTile) te).getStackInSlot(0);
                if (!i.isEmpty()) return i;
            }
        }
        return new ItemStack(this, 1);

    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof PedestalBlockTile) {
            PedestalBlockTile te = (PedestalBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            boolean flag1 = (te.isEmpty() && !itemstack.isEmpty() && (te.canInsertItem(0, itemstack, null)));
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
                    te.yaw=player.getHorizontalFacing().getAxis() == Direction.Axis.X ? 90 : 0;
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

        boolean up = state.get(UP);
        boolean down = state.get(DOWN);
        if(!up){
            if(!down){
                return VoxelShapes.or(VoxelShapes.create(0.1875D, 0.125D, 0.1875D, 0.815D, 0.885D, 0.815D),
                        VoxelShapes.create(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D),
                        VoxelShapes.create(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D)
                );
            }
            else{
                return VoxelShapes.or(VoxelShapes.create(0.1875D, 0, 0.1875D, 0.815D, 0.885D, 0.815D),
                        VoxelShapes.create(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D)
                );
            }
        }
        else{
            if(!down){
                return VoxelShapes.or(VoxelShapes.create(0.1875D, 0.125D, 0.1875D, 0.815D, 1, 0.815D),
                        VoxelShapes.create(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D)
                );
            }
            else{
                return VoxelShapes.create(0.1875D, 0, 0.1875D, 0.815D, 1, 0.815D);
            }
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
        return new PedestalBlockTile();
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
            if (tileentity instanceof PedestalBlockTile) {
                InventoryHelper.dropInventoryItems(world, pos, (PedestalBlockTile) tileentity);
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
        if (tileentity instanceof PedestalBlockTile)
            return Container.calcRedstoneFromInventory((PedestalBlockTile) tileentity);
        else
            return 0;
    }
}