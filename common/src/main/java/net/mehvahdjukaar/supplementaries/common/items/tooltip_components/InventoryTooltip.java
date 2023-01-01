package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;

public record InventoryTooltip(CompoundTag tag, Item item, int size) implements TooltipComponent {
}
