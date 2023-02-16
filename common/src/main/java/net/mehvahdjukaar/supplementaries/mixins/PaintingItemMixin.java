package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.PaintingTooltip;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(HangingEntityItem.class)
public abstract class PaintingItemMixin extends Item {

    protected PaintingItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (this == Items.PAINTING && ClientConfigs.Tweaks.PAINTINGS_TOOLTIPS.get()) {
            var tag = stack.getTag();
            if (tag != null) {
                var nbt = tag.getCompound("EntityTag");
                if (nbt != null) {
                    var v = ResourceLocation.tryParse(tag.getString("variant"));
                    if (v != null) {
                        var variant = Registry.PAINTING_VARIANT.getOptional(v);
                        return variant.map(PaintingTooltip::new);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
