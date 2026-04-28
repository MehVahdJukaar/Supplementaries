package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;

public class MapAtlasCompat {

    @PlatformImpl
    @Contract
    public static boolean canPlayerSeeDeathMarker(Player player) {
        throw new AssertionError();
    }
}
