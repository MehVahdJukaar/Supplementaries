package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.PaintingTooltip;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import static net.minecraft.world.entity.decoration.Painting.VARIANT_MAP_CODEC;

@Mixin(HangingEntityItem.class)
public abstract class PaintingItemMixin extends Item {

    protected PaintingItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        //TODO: use event
        if (ClientConfigs.Tweaks.PAINTINGS_TOOLTIPS.get()) {
          var data =  stack.get(DataComponents.ENTITY_DATA);
            if (data != null && data.contains("variant")) {
              var painting =  data.read(VARIANT_MAP_CODEC);
                if (painting.isSuccess()) {
                    return Optional.of(new PaintingTooltip(painting.result().get()));
                }
            }
        }
        return Optional.empty();
    }
}
