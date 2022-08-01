package net.mehvahdjukaar.supplementaries.integration.forge;

import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WaystonesCompatImpl {
    public static boolean isWaystone(BlockEntity blockEntity) {
        return blockEntity instanceof WaystoneBlockEntityBase;
    }

    @Nullable
    public static Component getName(BlockEntity te) {
        var s = ((WaystoneBlockEntityBase) te).getWaystone().getName();
        if (s.isEmpty()) return null;
        return Component.literal(s);
    }
}
