package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CuriosCompat;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemsUtilImpl {
    public static boolean extractFromContainerItemIntoSlot(Player player, ItemStack containerStack, Slot slot) {
        return false;
    }

    public static boolean addToContainerItem(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
        return false;
    }

    public static float getEncumbermentFromInventory(ItemStack stack, ServerPlayer player) {
        float amount = 0;
        var inventory = player.getInventory();

        for (int idx = 0; idx < inventory.getContainerSize(); idx++) {
            ItemStack slotItem = inventory.getItem(idx);
            amount += SackItem.getEncumber(slotItem);
        }
        if (CompatHandler.QUARK) {
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.CHEST);
            amount += QuarkCompat.getEncumbermentFromBackpack(backpack);
        }
        return amount;
    }


    public static KeyLockableTile.KeyStatus hasKeyInInventory(Player player, String key) {
        if (key == null) return KeyLockableTile.KeyStatus.CORRECT_KEY;
        KeyLockableTile.KeyStatus found = KeyLockableTile.KeyStatus.NO_KEY;
        if (CompatHandler.CURIOS) {
            found = CuriosCompat.isKeyInCurio(player, key);
            if (found == KeyLockableTile.KeyStatus.CORRECT_KEY) return found;
        }
        var inventory = player.getInventory();
        for (int idx = 0; idx < inventory.getContainerSize(); idx++) {
            ItemStack stack = inventory.getItem(idx);
            KeyLockableTile.KeyStatus status = IKeyLockable.getKeyStatus(stack, key);
            if (status == KeyLockableTile.KeyStatus.CORRECT_KEY) {
                return status;
            } else if (status == KeyLockableTile.KeyStatus.INCORRECT_KEY) {
                found = status;
            }
        }

        return found;
    }

    public static ItemStack removeFirstStackFromInventory(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        if (tile instanceof Container container) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack itemstack = container.getItem(slot);
                if (!itemstack.isEmpty()) {
                    ItemStack extracted = container.removeItem(slot, 1);
                    //empty stack means it can't extract from inventory
                    if (!extracted.isEmpty()) {
                        tile.setChanged();
                        return extracted.copy();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
