package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record SelectableContainerTooltip(List<ItemStack> stacks, int selected) implements TooltipComponent {
}