package net.mehvahdjukaar.supplementaries.client.gui;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SackSlot extends Slot {
    public SackSlot(IInventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return CommonUtil.isAllowedInShulker(stack);
    }
}
