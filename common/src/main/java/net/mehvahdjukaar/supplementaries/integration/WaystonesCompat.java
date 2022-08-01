package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WaystonesCompat {
    @ExpectPlatform
    public static boolean isWaystone(BlockEntity te) {
        throw new ArrayStoreException();
    }
    @Nullable
    @ExpectPlatform
    public static Component getName(BlockEntity te) {
        throw new ArrayStoreException();
    }
}
