package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.minecraft.world.entity.player.Player;

public class CurioCompat {

    //TODO: fix when there are multiple keys
    @ExpectPlatform
    public static KeyLockableTile.KeyStatus isKeyInCurio(Player player, String key) {
        throw new AssertionError();
    }
}
