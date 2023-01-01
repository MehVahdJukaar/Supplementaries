package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.BannerPattern;

public record BannerPatternTooltip(TagKey<BannerPattern> pattern) implements TooltipComponent {
}
