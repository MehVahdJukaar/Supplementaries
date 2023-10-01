package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.KeyItem;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public interface IKeyLockable {

    int MAX_ITEM_NAME_LEN = 50;

    void setPassword(String password);

    String getPassword();

    void clearPassword();

    default void onPasswordCleared(Player player, BlockPos pos) {
        player.displayClientMessage(Component.translatable("message.supplementaries.safe.cleared"), true);
        player.level().playSound(null, pos,
                SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
    }


    default boolean shouldShowPassword() {
        String password = this.getPassword();
        return password != null && password.length() <= MAX_ITEM_NAME_LEN;
    }

    default void onKeyAssigned(Level level, BlockPos pos, Player player, String newKey) {
        Component message;
        if (shouldShowPassword()) {
            message = Component.translatable("message.supplementaries.safe.assigned_key", newKey);
        } else message = Component.translatable("message.supplementaries.safe.assigned_key_generic");
        player.displayClientMessage(message, true);
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
    }


    default KeyLockableTile.KeyStatus getKeyStatus(ItemStack key) {
        return getKeyStatus(key, this.getPassword());
    }


    static KeyLockableTile.KeyStatus getKeyStatus(ItemStack key, String password) {
        String correct = getKeyPassword(key);
        if (correct != null) {
            if (correct.equals(password)) return KeyLockableTile.KeyStatus.CORRECT_KEY;
            else return KeyLockableTile.KeyStatus.INCORRECT_KEY;
        }
        return KeyLockableTile.KeyStatus.NO_KEY;
    }

    @Nullable
    static String getKeyPassword(ItemStack key) {
        if (key.getItem() instanceof KeyItem k) {
            return k.getPassword(key);
        } else if (key.is(ModTags.KEY)) {
            //default get name behavior
            return ModRegistry.KEY_ITEM.get().getPassword(key);
        }
        return null;
    }

    default boolean testIfHasCorrectKey(Player player, String lockPassword, boolean feedbackMessage, @Nullable String translName) {
        KeyStatus key = ItemsUtil.hasKeyInInventory(player, lockPassword);
        if (key == KeyStatus.INCORRECT_KEY) {
            if (feedbackMessage)
                player.displayClientMessage(Component.translatable("message.supplementaries.safe.incorrect_key"), true);
            return false;
        } else if (key == KeyStatus.CORRECT_KEY) return true;
        if (feedbackMessage)
            player.displayClientMessage(Component.translatable("message.supplementaries." + translName + ".locked"), true);
        return false;
    }



    enum KeyStatus {
        CORRECT_KEY,
        INCORRECT_KEY,
        NO_KEY;

        public boolean isCorrect() {
            return this == CORRECT_KEY;
        }
    }
}
