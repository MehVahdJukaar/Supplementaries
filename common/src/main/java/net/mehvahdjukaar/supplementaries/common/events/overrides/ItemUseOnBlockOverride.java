package net.mehvahdjukaar.supplementaries.common.events.overrides;


import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

interface ItemUseOnBlockOverride extends ItemUseOverride {


    /**
     * Used for permission checks on flan compat
     */
    default boolean altersWorld() {
        return false;
    }

    default boolean shouldBlockMapToItem(Item item) {
        return appliesToItem(item);
    }

    @Override
    @Nullable
    default MutableComponent getTooltip() {
        return null;
    }

    default boolean placesBlock(){
        return false;
    }
}
