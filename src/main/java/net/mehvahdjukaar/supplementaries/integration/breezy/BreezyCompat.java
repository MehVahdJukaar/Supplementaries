package net.mehvahdjukaar.supplementaries.integration.breezy;

import coda.breezy.common.WindDirectionSavedData;
import coda.breezy.networking.BreezyNetworking;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BreezyCompat {

    public static float getWindDirection(BlockPos pos, Level level) {
        WindDirectionSavedData data = BreezyNetworking.CLIENT_CACHE;
        if (data != null) {
            return data.getWindDirection(pos.getY(), level).toYRot();
        }
        return 90;
    }

}
