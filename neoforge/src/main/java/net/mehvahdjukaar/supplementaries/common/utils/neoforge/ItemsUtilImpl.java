package net.mehvahdjukaar.supplementaries.common.utils.neoforge;

import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.components.SafeOwner;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.neoforge.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class ItemsUtilImpl {

    public static boolean extractFromContainerItemIntoSlot(Player player, ItemStack containerStack, Slot slot) {
        if (slot.mayPickup(player) && containerStack.getCount() == 1) {

            IItemHandler handler = getItemHandler(containerStack, player);
            if (handler != null) {
                for (int s = 0; s < handler.getSlots(); s++) {
                    ItemStack selected = handler.getStackInSlot(s);
                    if (!selected.isEmpty()) {
                        ItemStack dropped = handler.extractItem(s, 1, false);

                        if (slot.mayPlace(dropped)) {
                            slot.set(dropped);
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static boolean addToContainerItem(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
        if (slot.mayPickup(player) && containerStack.getCount() == 1) {

            IItemHandler handler = getItemHandler(containerStack, player);
            if (handler != null) {
                ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
                boolean success = result.isEmpty() || result.getCount() != stack.getCount();
                if (success) {
                    if (simulate) {
                        return true;
                    } else {
                        //this is a mess and probably not even correct
                        CompoundTag newTag = new CompoundTag();
                        if (inSlot) {
                            stack.setCount(result.getCount());
                            ItemStack newStack = containerStack.copy();
                            if (slot.mayPlace(newStack)) {
                                slot.set(newStack);
                                return true;
                            }
                        } else {
                            int i = stack.getCount() - result.getCount();
                            slot.safeTake(i, i, player);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static IItemHandler getItemHandler(ItemStack containerStack, Player player) {
        IItemHandler itemHandler = containerStack.getCapability(Capabilities.ItemHandler.ITEM);
        if (itemHandler != null) {
            SafeOwner safeOwer = containerStack.get(ModComponents.SAFE_OWNER.get());
            //TODO: safe lock cap. also on fabric
            if (safeOwer != null && !safeOwer.canPlayerOpen(player)) return null;
            return itemHandler;
        }
        return null;
    }

    public static float getEncumbermentFromInventory(ItemStack stack, ServerPlayer player) {
        float amount = 0;
        AtomicReference<IItemHandler> reference = new AtomicReference<>();
        player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(reference::set);
        if (reference.get() != null) {
            for (int idx = 0; idx < reference.get().getSlots(); idx++) {
                ItemStack slotItem = reference.get().getStackInSlot(idx);
                amount += SackItem.getEncumber(slotItem);
            }

            if (CompatHandler.QUARK) {
                ItemStack backpack = player.getItemBySlot(EquipmentSlot.CHEST);
                amount += QuarkCompat.getEncumbermentFromBackpack(backpack);
            }
        }
        return amount;
    }

    public static KeyLockableTile.KeyStatus hasKeyInInventory(Player player, String key) {
        if (key == null) return KeyLockableTile.KeyStatus.CORRECT_KEY;
        KeyLockableTile.KeyStatus found = CompatHandler.getKeyFromModsSlots(player, key);
        if (found == KeyLockableTile.KeyStatus.CORRECT_KEY) return found;

        AtomicReference<IItemHandler> itemHandler = new AtomicReference<>();
        player.getCapability(Capabilities.ItemHandler.ENTITY).ifPresent(itemHandler::set);
        if (itemHandler.get() != null) {
            for (int idx = 0; idx < itemHandler.get().getSlots(); idx++) {
                ItemStack stack = itemHandler.get().getStackInSlot(idx);
                KeyLockableTile.KeyStatus status = IKeyLockable.getKeyStatus(stack, key);
                if (status == KeyLockableTile.KeyStatus.CORRECT_KEY) {
                    return status;
                } else if (status == KeyLockableTile.KeyStatus.INCORRECT_KEY) {
                    found = status;
                }
            }
        }
        return found;
    }

    public static ItemStack tryExtractingItem(Level level, Direction dir, Object tile) {
        if (tile instanceof ICapabilityProvider cp) {
            IItemHandler itemHandler = CapabilityHandler.get(cp, Capabilities.ItemHandler.ITEM_HANDLER, dir);
            if (itemHandler != null) {
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    ItemStack itemstack = itemHandler.getStackInSlot(slot);
                    if (!itemstack.isEmpty()) {
                        ItemStack extracted = itemHandler.extractItem(slot, 1, false);
                        //empty stack means it can't extract from inventory
                        if (!extracted.isEmpty()) {
                            if (cp instanceof Container c) c.setChanged();
                            return extracted.copy();
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack tryAddingItem(ItemStack stack, Level level, @Nullable Direction dir, Object container) {
        IItemHandler itemHandler = CapabilityHandler.get(cp, ForgeCapabilities.ITEM_HANDLER, dir);
        if (container instanceof AbstractChestedHorse && itemHandler instanceof IItemHandlerModifiable im) {
            //thanks...
            itemHandler = new RangedWrapper(im, 1, itemHandler.getSlots());
        }
        if (itemHandler != null) {
            return ItemHandlerHelper.insertItem(itemHandler, stack, false);
        }
        return stack;
    }


}
