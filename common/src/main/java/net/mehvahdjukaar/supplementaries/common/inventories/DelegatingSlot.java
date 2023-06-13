package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

public class DelegatingSlot extends Slot {
    public DelegatingSlot(BaseContainerBlockEntity inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.container.canPlaceItem(this.index, stack);
    }
}
