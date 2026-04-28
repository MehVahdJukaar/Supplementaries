package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WaystonesCompat {
    @PlatformImpl
    public static boolean isWaystone(@Nullable BlockEntity te) {
        throw new ArrayStoreException();
    }

    @Nullable
    @PlatformImpl
    public static Component getName(BlockEntity te) {
        throw new ArrayStoreException();
    }
}
