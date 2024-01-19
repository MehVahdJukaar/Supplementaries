package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.block.WaystoneBlockEntity;

public class WaystonesCompatImpl {

    public static boolean isWaystone(BlockEntity te) {
        return te instanceof WaystoneBlockEntity;
    }

    @Nullable
    public static Component getName(BlockEntity te) {
        var s = ((WaystoneBlockEntity) te).getWaystoneName();
        if (s.isEmpty()) return null;
        return Component.literal(s);
    }
}
