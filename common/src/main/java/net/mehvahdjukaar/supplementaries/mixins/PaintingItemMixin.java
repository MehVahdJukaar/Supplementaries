package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.PaintingTooltip;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(HangingEntityItem.class)
public abstract class PaintingItemMixin extends Item {

    @Shadow
    @Final
    private EntityType<? extends HangingEntity> type;

    protected PaintingItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        //TODO: use event
        if (ClientConfigs.Tweaks.PAINTINGS_TOOLTIPS.get() && type == EntityType.PAINTING) {
            var tag = stack.getTag();
            if (tag != null && tag.contains("EntityTag")) {
                var v = ResourceLocation.tryParse(tag.getCompound("EntityTag").getString("variant"));
                if (v != null) {
                    var variant = BuiltInRegistries.PAINTING_VARIANT.getOptional(v);
                    return variant.map(PaintingTooltip::new);
                }
            }
        }
        return Optional.empty();
    }
}
