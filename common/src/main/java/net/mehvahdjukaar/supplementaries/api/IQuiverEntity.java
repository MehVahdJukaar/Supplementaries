package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.world.item.ItemStack;

public interface IQuiverEntity {

    //only used for rendering for player and both for skeletons
    ItemStack supplementaries$getQuiver();

    default boolean supplementaries$hasQuiver(){
        return !this.supplementaries$getQuiver().isEmpty();
    }

    void supplementaries$setQuiver(ItemStack quiver);
}
