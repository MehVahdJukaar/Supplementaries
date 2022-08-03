package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CuriosCompat;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.concurrent.atomic.AtomicReference;

public class ItemsUtilImpl {
    @javax.annotation.Nullable
    public static boolean extractFromContainerItemIntoSlot(Player player, ItemStack containerStack, Slot slot) {
        return false;
    }

    @javax.annotation.Nullable
    public static boolean addToContainerItem(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
        return false;
    }

    public static int getAllSacksInInventory(ItemStack stack, ServerPlayer player, int amount) {
        var inventory = player.getInventory();

        for (int idx = 0; idx < inventory.getContainerSize(); idx++) {
            ItemStack slotItem = inventory.getItem(idx);
            if (slotItem.getItem() instanceof SackItem) {
                CompoundTag tag = slotItem.getTag();
                if (tag != null && tag.contains("BlockEntityTag")) {
                    amount++;
                }
            }
        }
        if (CompatHandler.quark) {
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.CHEST);
            amount += QuarkCompat.getSacksInBackpack(backpack);
        }
        return amount;
    }

    public static KeyLockableTile.KeyStatus hasKeyInInventory(Player player, String key) {
        if (key == null) return KeyLockableTile.KeyStatus.CORRECT_KEY;
        KeyLockableTile.KeyStatus found = KeyLockableTile.KeyStatus.NO_KEY;
        if (CompatHandler.curios) {
            found = CuriosCompat.isKeyInCurio(player, key);
            if (found == KeyLockableTile.KeyStatus.CORRECT_KEY) return found;
        }
        var inventory = player.getInventory();
        for (int idx = 0; idx < inventory.getContainerSize(); idx++) {
            ItemStack stack = inventory.getItem(idx);
            if (stack.is(ModTags.KEY)) {
                found = KeyLockableTile.KeyStatus.INCORRECT_KEY;
                if (KeyLockableTile.isCorrectKey(stack, key)) return KeyLockableTile.KeyStatus.CORRECT_KEY;
            }
        }

        return found;
    }

    public static boolean faucetSpillItems(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        return false;
    }
}
