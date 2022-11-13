package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.misc.BannerPatternTooltip;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(BannerPatternItem.class)
public abstract class BannerPatternItemMixin extends Item {


    @Shadow @Final private TagKey<BannerPattern> bannerPattern;

    protected BannerPatternItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if(ClientConfigs.Tweaks.BANNER_PATTERN_TOOLTIP.get()) {
            return Optional.of(new BannerPatternTooltip(this.bannerPattern));
        }
        return Optional.empty();
    }
}
