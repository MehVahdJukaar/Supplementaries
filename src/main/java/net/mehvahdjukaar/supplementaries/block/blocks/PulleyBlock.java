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

import net.minecraft.block.AbstractBlock.Properties;

public class PulleyBlock extends RotatedPillarBlock {
    public static final EnumProperty<Winding> TYPE = BlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = BlockProperties.FLIPPED;

    public PulleyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(TYPE, Winding.NONE).setValue(FLIPPED,false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE,FLIPPED);
    }

    //rotates itself and pull up/down. unchecked
    //all methods here are called server side
    public boolean axisRotate(BlockState state, BlockPos pos, World world, Rotation rot){
        world.setBlockAndUpdate(pos,state.cycle(FLIPPED));
        if(rot==Rotation.CLOCKWISE_90) return this.pullUp(pos, world,1);
        else return this.pullDown(pos, world,1);
    }

    public boolean pullUp(BlockPos pos, IWorld world, int rot){
        TileEntity tile = world.getBlockEntity(pos);
        if(tile instanceof PulleyBlockTile){
            if(!(world instanceof World))return false;
            ItemStack stack = ((ItemDisplayTile) tile).getDisplayedItem();
            boolean flag = false;
            if(stack.isEmpty()){
                stack = new ItemStack(world.getBlockState(pos.below()).getBlock().asItem());
                flag = true;
            }
            if(stack.getCount()+rot>stack.getMaxStackSize() || !(stack.getItem() instanceof BlockItem)) return false;
            Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
            boolean success = RopeBlock.removeRope(pos.below(), (World) world,ropeBlock);
            if(success){
                SoundType soundtype = ropeBlock.defaultBlockState().getSoundType(world, pos, null);
                world.playSound(null, pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if(flag)((ItemDisplayTile) tile).setDisplayedItem(stack);
                else stack.grow(1);
                tile.setChanged();
            }
            return success;
        }
        return false;
    }

    public boolean pullDown(BlockPos pos, IWorld world, int rot){
        TileEntity tile = world.getBlockEntity(pos);
        if(tile instanceof PulleyBlockTile){
            if(!(world instanceof World))return false;
            ItemStack stack = ((ItemDisplayTile) tile).getDisplayedItem();
            if(stack.getCount()<rot || !(stack.getItem() instanceof BlockItem)) return false;
            Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
            boolean success = RopeBlock.addRope(pos.below(), (World) world,null,Hand.MAIN_HAND,ropeBlock);
            if(success){
                SoundType soundtype = ropeBlock.defaultBlockState().getSoundType(world, pos, null);
                world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
                tile.setChanged();
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
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof PulleyBlockTile) {
            if (player instanceof ServerPlayerEntity) {
                if(!(player.isShiftKeyDown()&&this.axisRotate(state,pos,worldIn,Rotation.COUNTERCLOCKWISE_90)))
                    player.openMenu((INamedContainerProvider)tileentity);
            }
            return ActionResultType.sidedSuccess(worldIn.isClientSide());
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
        return new PulleyBlockTile();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof IInventory) {
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