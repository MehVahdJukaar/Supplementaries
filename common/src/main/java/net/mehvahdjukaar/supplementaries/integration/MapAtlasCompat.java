package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;

public class MapAtlasCompat {

    @ExpectPlatform
    @Contract
    public static boolean canPlayerSeeDeathMarker(Player player){
        throw new AssertionError();
    }
}
