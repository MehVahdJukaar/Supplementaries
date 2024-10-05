package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.CustomData;

public record PaintingTooltip(CustomData data) implements TooltipComponent {
}
