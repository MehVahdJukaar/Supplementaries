package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.BlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.inventories.PulleyBlockContainer;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;


public class PulleyBlockTile extends ItemDisplayTile {

    public PulleyBlockTile() {
        super(Registry.PULLEY_BLOCK_TILE.get());
    }

    @Override
    public void updateOnChanged() {}

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
        if(item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ChainBlock || ModTags.isTagged(ModTags.CHAINS,item))type = Winding.CHAIN;
        else if(ModTags.isTagged(ModTags.ROPES,item))type = Winding.ROPE;
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

}