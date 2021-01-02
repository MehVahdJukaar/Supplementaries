package net.mehvahdjukaar.supplementaries.gui;

import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SackSlot extends SlotItemHandler {
    public SackSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if((Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)||
                (stack.getItem() instanceof SackItem))return false;
        return super.isItemValid(stack);
    }
}
