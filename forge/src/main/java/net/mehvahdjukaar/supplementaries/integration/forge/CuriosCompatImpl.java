package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;

public class CuriosCompatImpl {
    public static KeyLockableTile.KeyStatus isKeyInCurio(Player player, String password) {
        var found = CuriosApi.getCuriosHelper().findCurios(player, i -> i.is(ModTags.KEY));
        if (found.isEmpty()) return KeyLockableTile.KeyStatus.NO_KEY;
        else {
            for (var slot : found) {
                ItemStack stack = slot.stack();
                if (KeyLockableTile.isCorrectKey(stack, password)) return KeyLockableTile.KeyStatus.CORRECT_KEY;
            }
            return KeyLockableTile.KeyStatus.INCORRECT_KEY;
        }
    }

    @Nullable
    public static ItemStack getEquippedQuiver(Player player) {
        var found = CuriosApi.getCuriosHelper().findCurios(player, i -> i.is(ModRegistry.QUIVER_ITEM.get()));
        for (var slot : found) {
            return slot.stack();
        }
        return null;
    }

}
