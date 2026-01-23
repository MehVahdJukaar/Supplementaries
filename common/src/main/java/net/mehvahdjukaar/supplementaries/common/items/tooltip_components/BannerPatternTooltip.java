package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.Optional;

public record BannerPatternTooltip(Holder<BannerPattern> pattern) implements TooltipComponent {


    public static BannerPatternTooltip create(TagKey<BannerPattern> patternTag) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        var reg = level.registryAccess().registryOrThrow(
                Registries.BANNER_PATTERN
        );
        Optional<Holder<BannerPattern>> bannerPatternHolder = reg.getTag(patternTag)
                .flatMap(n -> n.stream().findAny());

        return bannerPatternHolder.map(BannerPatternTooltip::new).orElse(null);
    }
}
