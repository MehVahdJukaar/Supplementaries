package net.mehvahdjukaar.supplementaries.integration.platform;

import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.world.entity.player.Player;

public class TrinketsCompatImpl {

    public static void init() {
    }

    public static SlotReference getQuiver(Player arg0) {
        return SlotReference.EMPTY;
    }

    public static KeyLockableTile.KeyStatus getKey(Player arg0, String arg1) {
        return IKeyLockable.KeyStatus.NO_KEY;
    }
}
