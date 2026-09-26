package net.mehvahdjukaar.supplementaries.integration.platform;

import net.minecraft.world.entity.player.Player;
import pepjebs.mapatlases.api.MapAtlasesApi;

public class MapAtlasCompatImpl {
    public static boolean canPlayerSeeDeathMarker(Player player) {
        return MapAtlasesApi.canPlayerSeeDeathMarker(player);
    }
}
