package net.mehvahdjukaar.supplementaries.common.inventories;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DelegatingSlot extends Slot {
    private final Runnable onChange;

    public DelegatingSlot(Container inventory, int index, int xPosition, int yPosition, AbstractContainerMenu menu) {
        super(inventory, index, xPosition, yPosition);
        this.onChange = () -> menu.slotsChanged(inventory);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.container.canPlaceItem(this.index, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChange.run();
    }
}
