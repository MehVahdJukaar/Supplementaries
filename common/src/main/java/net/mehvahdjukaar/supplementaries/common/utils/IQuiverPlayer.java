package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;

public interface IQuiverPlayer extends IQuiverEntity {

    SlotReference supplementaries$getQuiverSlot();

    void supplementaries$setQuiverSlot(SlotReference slot);
}
