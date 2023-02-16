package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PaintingTooltip(PaintingVariant pattern) implements TooltipComponent {
}
