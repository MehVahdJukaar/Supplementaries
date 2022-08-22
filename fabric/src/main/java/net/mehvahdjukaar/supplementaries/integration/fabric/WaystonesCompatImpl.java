package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WaystonesCompatImpl {
    public static boolean isWaystone(BlockEntity te) {
        return te != null && Utils.getID(te.getBlockState().getBlock()).getPath().contains("waystone");
    }

    @Nullable
    public static Component getName(BlockEntity te) {
        return null;
    }
}
