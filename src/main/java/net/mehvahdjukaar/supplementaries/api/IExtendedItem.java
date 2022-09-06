package net.mehvahdjukaar.supplementaries.api;

import javax.annotation.Nullable;

public interface IExtendedItem {

    @Nullable
    IAdditionalPlacement getAdditionalBehavior();

    void addAdditionalBehavior(IAdditionalPlacement b);

    default boolean hasPlacementBehavior(){
        return getAdditionalBehavior() != null;
    }
}
