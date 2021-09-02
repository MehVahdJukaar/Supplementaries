package net.mehvahdjukaar.supplementaries.compat.curios;

import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class SupplementariesCuriosPlugin {

    public static KeyLockableTile.KeyStatus isKeyInCurio(PlayerEntity player, String password) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> found = CuriosApi.getCuriosHelper().findEquippedCurio(ModRegistry.KEY_ITEM.get(), player);
        KeyLockableTile.KeyStatus key = KeyLockableTile.KeyStatus.NO_KEY;
        if(found.isPresent()){
            key = KeyLockableTile.KeyStatus.INCORRECT_KEY;
            ItemStack stack = found.get().right;
            if(KeyLockableTile.isCorrectKey(stack, password))return KeyLockableTile.KeyStatus.CORRECT_KEY;
        }
        return key;
    }
}
