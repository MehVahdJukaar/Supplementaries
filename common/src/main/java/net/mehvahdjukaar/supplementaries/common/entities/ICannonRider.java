package net.mehvahdjukaar.supplementaries.common.entities;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ICannonRider {

    @Nullable
    BlockPos supplementaries$getCannonPos();

    void supplementaries$setCannonPos(@Nullable BlockPos pos);

    CannonFlight supplementaries$getCannonFlight();
}
