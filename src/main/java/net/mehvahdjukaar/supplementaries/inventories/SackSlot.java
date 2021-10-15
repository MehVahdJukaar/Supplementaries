package net.mehvahdjukaar.supplementaries.inventories;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SackSlot extends Slot {
    public SackSlot(Container inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return CommonUtil.isAllowedInShulker(stack);
    }
}
