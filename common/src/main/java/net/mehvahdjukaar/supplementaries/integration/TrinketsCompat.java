package net.mehvahdjukaar.supplementaries.integration;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.KeyItem;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

//this is actually for trinkets
public class TrinketsCompat {

    static KeyLockableTile.KeyStatus getKey(Player player, String password) {

        TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinket != null) {
            var found = trinket.getEquipped(i ->
                    i.is(ModTags.KEYS) || i.getItem() instanceof KeyItem);
            if (found.isEmpty()) return KeyLockableTile.KeyStatus.NO_KEY;
            for (var slot : found) {
                ItemStack stack = slot.getB();
                if (IKeyLockable.getKeyStatus(stack, password).isCorrect()) {
                    return KeyLockableTile.KeyStatus.CORRECT_KEY;
                }
            }
            return KeyLockableTile.KeyStatus.INCORRECT_KEY;
        }
        return IKeyLockable.KeyStatus.NO_KEY;
    }

    static SlotReference getQuiver(Player player) {
        TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinket != null) {
            var found = trinket.getEquipped(ModRegistry.QUIVER_ITEM.get());
            if (!found.isEmpty()) {
                var r = found.get(0).getA();
                return () -> r.inventory().getItem(r.index());
            }
        }
        return SlotReference.EMPTY;
    }
}
