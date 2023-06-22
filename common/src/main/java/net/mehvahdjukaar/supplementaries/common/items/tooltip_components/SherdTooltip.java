package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record SherdTooltip(ResourceKey<String> sherd) implements TooltipComponent {
}
