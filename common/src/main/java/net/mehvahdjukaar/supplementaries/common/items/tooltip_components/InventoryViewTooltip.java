package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

public record InventoryViewTooltip(ItemContainerContents contents, int size,
                                   @Nullable DyeColor color) implements TooltipComponent {
}
