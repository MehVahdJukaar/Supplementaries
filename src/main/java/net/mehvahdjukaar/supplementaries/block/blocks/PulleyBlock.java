package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.block.tiles.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.PulleyBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class PulleyBlock extends RotatedPillarBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Winding> TYPE = BlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = BlockProperties.FLIPPED;
    public PulleyBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y).with(TYPE, Winding.NONE).with(FLIPPED,false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(TYPE,FLIPPED);
    }

    //rotates itself and pull up/down. unchecked
    public boolean axisRotate(BlockState state, BlockPos pos, World world, Rotation rot){
        world.setBlockState(pos,state.func_235896_a_(FLIPPED));
        if(rot==Rotation.CLOCKWISE_90) return this.pullUp(pos, world,1);
        else return this.pullDown(pos, world,1);
    }

    public boolean pullUp(BlockPos pos, IWorld world, int rot){
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof PulleyBlockTile){
            if(!(world instanceof World))return false;
            ItemStack stack = ((ItemDisplayTile) tile).getDisplayedItem();
            if(stack.getCount()+rot>stack.getMaxStackSize() || !(stack.getItem() instanceof BlockItem)) return false;
            Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
            boolean success = RopeBlock.removeRope(pos.down(), (World) world,ropeBlock);
            if(success){
                SoundType soundtype = ropeBlock.getDefaultState().getSoundType(world, pos, null);
                world.playSound(null, pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.grow(1);
            }
            return success;
        }
        return false;
    }

    public boolean pullDown(BlockPos pos, IWorld world, int rot){
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof PulleyBlockTile){
            if(!(world instanceof World))return false;
            ItemStack stack = ((ItemDisplayTile) tile).getDisplayedItem();
            if(stack.getCount()<rot || !(stack.getItem() instanceof BlockItem)) return false;
            Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
            boolean success = RopeBlock.addRope(pos.down(), (World) world,null,Hand.MAIN_HAND,ropeBlock);
            if(success){
                SoundType soundtype = ropeBlock.getDefaultState().getSoundType(world, pos, null);
                world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
            }
            return success;
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof PulleyBlockTile) {
            if (player instanceof ServerPlayerEntity) {
                if(!(player.isSneaking()&&this.axisRotate(state,pos,worldIn,Rotation.COUNTERCLOCKWISE_90)))
                    player.openContainer((INamedContainerProvider)tileentity);
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote());
        }
        return ActionResultType.PASS;
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
        return new PulleyBlockTile();
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
            if (tileentity instanceof IInventory) {
                InventoryHelper.dropInventoryItems(world, pos, (IInventory) tileentity);
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
        if (tileentity instanceof NoticeBoardBlockTile)
            return Container.calcRedstoneFromInventory((IInventory) tileentity);
        else
            return 0;
    }
}