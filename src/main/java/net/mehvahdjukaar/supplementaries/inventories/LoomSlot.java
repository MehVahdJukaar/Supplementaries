package net.mehvahdjukaar.supplementaries.inventories;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;

public class LoomSlot extends Slot {
    public LoomSlot(IInventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof BannerItem || stack.getItem() instanceof FlagItem;
    }
}
