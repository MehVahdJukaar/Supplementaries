package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;

public record InventoryTooltip(ItemContainerContents contents, int size) implements TooltipComponent {
}
