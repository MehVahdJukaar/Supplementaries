package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.PaintingTooltip;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(HangingEntityItem.class)
public abstract class PaintingItemMixin extends Item {

    protected PaintingItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        //TODO: use event
        if (ClientConfigs.Tweaks.PAINTINGS_TOOLTIPS.get()) {
            CustomData customData = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
            if (!customData.isEmpty() && customData.contains("variant")) {
                return Optional.of(new PaintingTooltip(customData));
            }
        }
        return Optional.empty();
    }
}
