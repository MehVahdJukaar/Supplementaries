package net.mehvahdjukaar.supplementaries.gui;

import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SackSlot extends SlotItemHandler {
    public SackSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        ITag<Item> t = ItemTags.getCollection().get(Resources.SHULKER_BLACKLIST);
        if(t!=null && stack.getItem().isIn(t))
            return false;
        return super.isItemValid(stack);
    }
}
