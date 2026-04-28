package net.mehvahdjukaar.supplementaries.integration.platform;

import net.minecraft.world.entity.player.Player;
import pepjebs.mapatlases.integration.SupplementariesCompat;

public class MapAtlasCompatImpl {
    public static boolean canPlayerSeeDeathMarker(Player player) {
        return SupplementariesCompat.canPlayerSeeDeathMarker(player);
    }
}
