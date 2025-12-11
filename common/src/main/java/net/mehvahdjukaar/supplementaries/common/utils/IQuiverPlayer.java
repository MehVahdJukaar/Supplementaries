package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;

public interface IQuiverPlayer extends IQuiverEntity {

    SlotReference supplementaries$getQuiverSlot();

    void supplementaries$setQuiverSlot(SlotReference slot);
}
