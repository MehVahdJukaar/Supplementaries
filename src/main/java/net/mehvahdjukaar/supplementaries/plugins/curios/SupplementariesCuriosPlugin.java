package net.mehvahdjukaar.supplementaries.plugins.curios;

import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class SupplementariesCuriosPlugin {

    public static boolean isKeyInCurio(PlayerEntity player, String password, String translName) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> found = CuriosApi.getCuriosHelper().findEquippedCurio(Registry.KEY_ITEM.get(), player);
        if(found.isPresent()){
            ItemStack stack = found.get().right;
            return KeyLockableTile.isCorrectKey(stack, password);
        }
        return false;
    }
}
