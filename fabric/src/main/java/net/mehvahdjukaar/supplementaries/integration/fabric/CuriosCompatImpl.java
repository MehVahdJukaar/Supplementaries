package net.mehvahdjukaar.supplementaries.integration.fabric;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.KeyItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//this is actually for trinkets
public class CuriosCompatImpl {

    public static KeyLockableTile.KeyStatus isKeyInCurio(Player player, String password) {

        TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinket != null) {
            List<Tuple<SlotReference, ItemStack>> found = trinket.getEquipped(i ->
                    i.is(ModTags.KEY) || i.getItem() instanceof KeyItem);
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

    @Nullable
    public static ItemStack getQuiverInCurio(Player player) {
        TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinket != null) {
            List<Tuple<SlotReference, ItemStack>> found = trinket.getEquipped(ModRegistry.QUIVER_ITEM.get());
            if (!found.isEmpty()) return found.get(0).getB();
            return null;
        }
        return null;
    }
}
