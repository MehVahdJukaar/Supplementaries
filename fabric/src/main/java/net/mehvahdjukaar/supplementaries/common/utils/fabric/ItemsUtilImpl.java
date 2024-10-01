package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

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
        KeyLockableTile.KeyStatus found = CompatHandler.getKeyFromModsSlots(player, key);
        if (found == KeyLockableTile.KeyStatus.CORRECT_KEY) return found;
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

    public static ItemStack tryExtractingItem(Level level, @Nullable Direction direction, Object tile) {
        if (tile instanceof Container container) {
            int[] slots;
            if (container instanceof WorldlyContainer wc && direction != null) {
                slots = wc.getSlotsForFace(direction);
            } else {
                slots = IntStream.rangeClosed(0, container.getContainerSize()-1).toArray();
            }
            for (int slot : slots) {
                ItemStack itemStack = container.getItem(slot);
                if (!itemStack.isEmpty()) {
                    if (container instanceof WorldlyContainer wc && !wc.canTakeItemThroughFace(slot, itemStack, direction)) {
                        continue;
                    }
                    ItemStack extracted = container.removeItem(slot, 1);
                    //empty stack means it can't extract from inventory
                    if (!extracted.isEmpty()) {
                        container.setChanged();
                        return extracted.copy();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack tryAddingItem(ItemStack stack, Level level, @Nullable Direction direction, Object tile) {
        if (tile instanceof Container container) {
            int[] slots;
            if (container instanceof WorldlyContainer wc && direction != null) {
                slots = wc.getSlotsForFace(direction);
            } else {
                slots = IntStream.rangeClosed(0, container.getContainerSize()-1).toArray();
            }
            for (int i : slots) {
                stack = tryMoveInItem(container, stack, i, direction);
                if (stack.isEmpty()) return stack;
            }
        }
        return stack;
    }

    private static ItemStack tryMoveInItem(Container destination, ItemStack stack, int slot, @Nullable Direction direction) {
        ItemStack itemStack = destination.getItem(slot);
        if (destination instanceof WorldlyContainer worldlyContainer &&
                !worldlyContainer.canPlaceItemThroughFace(slot, stack, direction)) {
            return stack;
        }

        boolean bl = false;
        if (itemStack.isEmpty()) {
            destination.setItem(slot, stack);
            stack = ItemStack.EMPTY;
            bl = true;
        } else if (canMergeItems(itemStack, stack)) {
            int i = stack.getMaxStackSize() - itemStack.getCount();
            int j = Math.min(stack.getCount(), i);
            stack.shrink(j);
            itemStack.grow(j);
            bl = j > 0;
        }

        if (bl) {
            destination.setChanged();
        }
        return stack;
    }

    private static boolean canMergeItems(ItemStack stack1, ItemStack stack2) {
        return stack1.getCount() <= stack1.getMaxStackSize() && ItemStack.isSameItemSameComponents(stack1, stack2);
    }
}
