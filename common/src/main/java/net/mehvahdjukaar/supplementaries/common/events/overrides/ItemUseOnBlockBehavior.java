package net.mehvahdjukaar.supplementaries.common.events.overrides;


import net.minecraft.world.item.Item;

interface ItemUseOnBlockBehavior extends ItemUseBehavior {


    /**
     * Used for permission checks on flan compat
     */
    default boolean altersWorld() {
        return false;
    }

    default boolean shouldBlockMapToItem(Item item) {
        return appliesToItem(item);
    }

    default boolean placesBlock(){
        return false;
    }
}
