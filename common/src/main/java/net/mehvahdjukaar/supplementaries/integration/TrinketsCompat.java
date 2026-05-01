package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.world.entity.player.Player;

public class TrinketsCompat {

    @PlatformImpl
    public static void init() {
        throw new AssertionError();
    }

    @PlatformImpl
    static KeyLockableTile.KeyStatus getKey(Player player, String password) {
        throw new AssertionError();
    }

    @PlatformImpl
    static SlotReference getQuiver(Player player) {
        throw new AssertionError();
    }

}
