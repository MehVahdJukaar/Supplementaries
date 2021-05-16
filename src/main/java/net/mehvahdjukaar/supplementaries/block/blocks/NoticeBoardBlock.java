package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.NoticeBoardBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NoticeBoardBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    public NoticeBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_BOOK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOK);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);

        boolean server = !worldIn.isClientSide();
        if (tileentity instanceof NoticeBoardBlockTile) {
            ItemStack itemstack = player.getItemInHand(handIn);
            NoticeBoardBlockTile te = (NoticeBoardBlockTile) tileentity;


            //insert Item
            if (te.isEmpty() && (te.canPlaceItemThroughFace(0, itemstack, null))&& player.abilities.mayBuild) {
                if(server){
                    ItemStack it = itemstack.copy();
                    it.setCount(1);
                    te.setItems(NonNullList.withSize(1, it));
                    te.setChanged();
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F,
                            worldIn.random.nextFloat() * 0.10F + 0.95F);
                }
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
            }
            // change color
            else if (itemstack.getItem() instanceof DyeItem && player.abilities.mayBuild) {
                if(te.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server){
                        te.setChanged();
                    }
                }
            }
            //pop item
            else if (player.isShiftKeyDown() && !te.isEmpty()) {
                if(server){
                    ItemStack it = te.removeItemNoUpdate(0);
                    BlockPos newpos = pos.offset(state.getValue(FACING).getNormal());
                    ItemEntity drop = new ItemEntity(worldIn, newpos.getX() + 0.5, newpos.getY() + 0.5, newpos.getZ() + 0.5, it);
                    drop.setDefaultPickUpDelay();
                    worldIn.addFreshEntity(drop);
                    te.setChanged();
                }
            }
            //open gui
            else if (player instanceof ServerPlayerEntity) {
                player.openMenu((INamedContainerProvider)tileentity);
            }
            return ActionResultType.sidedSuccess(!server);
        }
        return ActionResultType.PASS;
    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new NoticeBoardBlockTile();
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        //only needed if you are not using block entity tag
        if (stack.hasCustomHoverName()) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof NoticeBoardBlockTile) {
                ((LockableTileEntity) tileentity).setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if(facing==stateIn.getValue(FACING)){
            TileEntity te = worldIn.getBlockEntity(currentPos);
            if(te instanceof NoticeBoardBlockTile){
                //((NoticeBoardBlockTile)te).textVisible = this.skipRendering(stateIn,facingState,facing);
                boolean culled = facingState.isSolidRender(worldIn,currentPos) &&
                        facingState.isFaceSturdy(worldIn, facingPos, facing.getOpposite());
                ((NoticeBoardBlockTile)te).textVisible=!culled;
            }
        }
        return stateIn;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof NoticeBoardBlockTile) {
                InventoryHelper.dropContents(world, pos, (IInventory) tileentity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof NoticeBoardBlockTile)
            return Container.getRedstoneSignalFromContainer((IInventory) tileentity);
        else
            return 0;
    }
}