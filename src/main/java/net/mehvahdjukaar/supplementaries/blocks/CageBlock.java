package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;


public class CageBlock extends Block {
    protected static final VoxelShape SHAPE = Block.makeCuboidShape(1D,0D,1D,15.0D,16.0D,15.0D);
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final IntegerProperty LIGHT_LEVEL = CommonUtil.LIGHT_LEVEL_0_15;
    public CageBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(LIGHT_LEVEL, 0).with(FACING, Direction.NORTH));
    }

    public ItemStack getCageItem(CageBlockTile te){
        ItemStack returnStack;
        if(te.hasNoMob()){
            returnStack = new ItemStack(Registry.EMPTY_CAGE_ITEM);
        }
        else{
            returnStack = new ItemStack(Registry.CAGE_ITEM);
            CommonUtil.saveJarMobItemNBT(returnStack, te.mob, 1f, 0.875f);
        }
        return returnStack;
    }

    // shulker box code
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof CageBlockTile) {
            CageBlockTile tile = (CageBlockTile) tileentity;
            if (!worldIn.isRemote && player.isCreative()) {

                ItemStack itemstack = this.getCageItem(tile);

                ItemEntity itementity = new ItemEntity(worldIn, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof CageBlockTile) {
            CageBlockTile tile = (CageBlockTile) tileentity;

            ItemStack itemstack = this.getCageItem(tile);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    //for pick block
    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof CageBlockTile) {
            CageBlockTile tile = (CageBlockTile) tileentity;
            return this.getCageItem(tile);
        }
        return super.getItem(worldIn, pos, state);
    }

    // end shoulker box code
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL,FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
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
        return new CageBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.get(LIGHT_LEVEL);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }


}