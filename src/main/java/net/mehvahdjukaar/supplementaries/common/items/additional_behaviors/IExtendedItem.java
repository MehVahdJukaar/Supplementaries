package net.mehvahdjukaar.supplementaries.common.items.additional_behaviors;

import javax.annotation.Nullable;

public interface IExtendedItem {

    @Nullable
    AdditionalPlacement getAdditionalBehavior();

    void addAdditionalBehavior(AdditionalPlacement b);

    default boolean hasPlacementBehavior(){
        return getAdditionalBehavior() != null;
    }
}
