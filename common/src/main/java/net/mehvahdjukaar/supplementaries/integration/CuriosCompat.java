package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class CuriosCompat {

    @ExpectPlatform
    public static KeyLockableTile.KeyStatus isKeyInCurio(Player player, String key) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static ItemStack getEquippedQuiver(Player player) {
        throw new AssertionError();
    }
}
