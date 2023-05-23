package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CuriosCompatImpl {
    public static KeyLockableTile.KeyStatus isKeyInCurio(Player player, String key) {
        return KeyLockableTile.KeyStatus.NO_KEY;
    }

    @org.jetbrains.annotations.Nullable
    public static ItemStack getEquippedQuiver(Player player) {
        return null;
    }
}
