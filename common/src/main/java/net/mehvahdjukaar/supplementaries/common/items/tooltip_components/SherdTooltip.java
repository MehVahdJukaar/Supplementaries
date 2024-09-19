package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;

public record SherdTooltip(ResourceKey<DecoratedPotPattern> sherd) implements TooltipComponent {
}
