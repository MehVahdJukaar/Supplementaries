package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.inventories.PulleyBlockContainer;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class PulleyBlockTile extends ItemDisplayTile {

    public PulleyBlockTile() {
        super(Registry.PULLEY_BLOCK_TILE.get());
    }

    @Override
    public void updateOnChangedBeforePacket() {}

    //hijacking this method to work with hoppers
    @Override
    public void setChanged() {
        if(this.level==null)return;
        this.updateTile();
       //this.updateServerAndClient();
        super.setChanged();
    }

    public void updateTile() {
        if(this.level.isClientSide)return;
        Winding type = getContentType(this.getDisplayedItem().getItem());
        BlockState state = this.getBlockState();
        if(state.getValue(PulleyBlock.TYPE)!=type){
            level.setBlockAndUpdate(this.worldPosition,state.setValue(PulleyBlock.TYPE,type));
        }
    }

    public static Winding getContentType(Item item){
        Winding type = Winding.NONE;
        if(item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ChainBlock || item.is(ModTags.CHAINS))type = Winding.CHAIN;
        else if(item.is(ModTags.ROPES))type = Winding.ROPE;
        return type;
    }


    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.pulley_block");
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return new PulleyBlockContainer(id, player,this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return (getContentType(stack.getItem())!=Winding.NONE);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }


    public boolean handleRotation(Rotation rot){
        if(rot==Rotation.CLOCKWISE_90) return this.pullUp(this.worldPosition, this.level,1);
        else return this.pullDown(this.worldPosition, this.level,1);
    }

    public boolean pullUp(BlockPos pos, IWorld world, int rot){

        if(!(world instanceof World))return false;
        ItemStack stack = this.getDisplayedItem();
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
            if(flag)this.setDisplayedItem(stack);
            else stack.grow(1);
            this.setChanged();
        }
        return success;
    }

    public boolean pullDown(BlockPos pos, IWorld world, int rot){

        if(!(world instanceof World))return false;
        ItemStack stack = this.getDisplayedItem();
        if(stack.getCount()<rot || !(stack.getItem() instanceof BlockItem)) return false;
        Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
        boolean success = RopeBlock.addRope(pos.below(), (World) world,null, Hand.MAIN_HAND,ropeBlock);
        if(success){
            SoundType soundtype = ropeBlock.defaultBlockState().getSoundType(world, pos, null);
            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            stack.shrink(1);
            this.setChanged();
        }
        return success;
    }

}