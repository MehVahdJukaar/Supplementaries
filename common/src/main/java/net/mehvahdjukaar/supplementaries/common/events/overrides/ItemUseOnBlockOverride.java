package net.mehvahdjukaar.supplementaries.common.events.overrides;


import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

interface ItemUseOnBlockOverride extends ItemUseOverride {

    default boolean shouldBlockMapToItem(Item item) {
        return appliesToItem(item);
    }

    @Override
    @Nullable
    default MutableComponent getTooltip() {
        return null;
    }
}
