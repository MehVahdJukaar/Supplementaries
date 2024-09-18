package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class PancakeItem extends Item {

    public PancakeItem(Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId() {
        return ModRegistry.PANCAKE.get().getDescriptionId();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        int oldAmount = itemStack.getCount();
        itemStack.setCount(1);
        var r = super.useOn(context);
        if (itemStack.isEmpty()) {
            itemStack.setCount(oldAmount - 1);
        } else itemStack.setCount(oldAmount);
        if (!r.consumesAction()) {
            return AdditionalItemPlacement.getBlockPlacer()
                    .mimicUseOn(context, ModRegistry.PANCAKE.get(), null);
        }
        return r;
    }
}
