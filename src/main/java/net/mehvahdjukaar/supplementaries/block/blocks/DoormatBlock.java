package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.DoormatBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.DoormatGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;


public class DoormatBlock extends Block {
    protected static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(0.0D, 0.0D, 2.0D, 16.0D, 1.0D, 14.0D);

    protected static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(2.0D, 0.0D, 0.0D, 14.0D, 1.0D, 16.0D);
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public DoormatBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof DoormatBlockTile) {
            DoormatBlockTile te = (DoormatBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            boolean server = !worldIn.isRemote();
            boolean flag = itemstack.getItem() instanceof DyeItem && player.abilities.allowEdit;
            boolean sideHit = hit.getFace()!=Direction.UP;
            boolean canExtract = itemstack.isEmpty() && (player.isSneaking()||sideHit);
            boolean canInsert = te.isEmpty() && sideHit;
            if(canExtract ^ canInsert){
                if(!server)return ActionResultType.SUCCESS;
                if(canExtract) {
                    ItemStack dropStack = te.removeStackFromSlot(0);
                    ItemEntity drop = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY() + 0.125, pos.getZ() + 0.5, dropStack);
                    drop.setDefaultPickupDelay();
                    worldIn.addEntity(drop);
                }
                else{
                    ItemStack newStack = itemstack.copy();
                    newStack.setCount(1);
                    te.setItems(NonNullList.withSize(1, newStack));
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                }
                te.markDirty();
                worldIn.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1.0F,
                        1.2f);
                return ActionResultType.CONSUME;

            }
            //color
            if (flag){
                if(te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server)te.markDirty();
                }
            }
            // open gui (edit sign with empty hand)
            else if (!server) {
                DoormatGui.open(te);
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return !worldIn.isAirBlock(pos.down());
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING).getAxis()) {
            default:
            case Z:
                return SHAPE_NORTH;
            case X :
                return SHAPE_WEST;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING,context.getPlacementHorizontalFacing());
    }

    //for player bed spawn
    @Override
    public boolean canSpawnInBlock() {
        return true;
    }

    /*
    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.OPEN;
    }*/

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DoormatBlockTile();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof IInventory) {
                InventoryHelper.dropInventoryItems(world, pos, (IInventory) tileentity);
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

}