package net.mehvahdjukaar.supplementaries.api;

import javax.annotation.Nullable;

public interface IExtendedItem {

    @Nullable
    AdditionalPlacement getAdditionalBehavior();

    void addAdditionalBehavior(AdditionalPlacement b);

    default boolean hasPlacementBehavior(){
        return getAdditionalBehavior() != null;
    }
}
