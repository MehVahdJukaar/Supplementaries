package net.mehvahdjukaar.supplementaries.integration.forge;

import codyhuh.breezy.common.WindDirectionSavedData;
import codyhuh.breezy.networking.BreezyNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.level.Level;

public class BreezyCompatImpl {

    public static float getWindDirection(BlockPos pos, Level level) {
        WindDirectionSavedData data = BreezyNetworking.CLIENT_CACHE;
        if (data != null) {
            return data.getWindDirection(pos.getY(), level).toYRot();
        }
        return 90;
    }
}
