package net.mehvahdjukaar.supplementaries.integration.forge;

import codyhuh.breezy.common.network.BreezyNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BreezyCompatImpl {

    public static float getWindDirection(BlockPos pos, Level level) {
        var data = BreezyNetworking.CLIENT_CACHE;
        if (data != null) {
            return (float) data.getWindAtHeight(pos.getY(), level);
        }
        return 90;
    }
}
