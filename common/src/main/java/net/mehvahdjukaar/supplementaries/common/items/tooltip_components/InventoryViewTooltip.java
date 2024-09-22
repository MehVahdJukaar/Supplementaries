package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemContainerContents;

public record InventoryViewTooltip(ItemContainerContents contents, int size) implements TooltipComponent {
}
