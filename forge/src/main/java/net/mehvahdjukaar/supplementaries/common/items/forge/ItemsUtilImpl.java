package net.mehvahdjukaar.supplementaries.common.items.forge;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.ItemsUtil;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class ItemsUtilImpl {

    @javax.annotation.Nullable
    public static boolean extractFromContainerItemIntoSlot(Player player, ItemStack containerStack, Slot slot) {
        if (slot.mayPickup(player)) {

            var handlerAndTe = getItemHandler(containerStack, player);
            if (handlerAndTe != null) {
                IItemHandler handler = handlerAndTe.getFirst();
                for (int s = 0; s < handler.getSlots(); s++) {
                    ItemStack selected = handler.getStackInSlot(s);
                    if (!selected.isEmpty()) {
                        ItemStack dropped = handler.extractItem(s, 1, false);

                        if (slot.mayPlace(dropped)) {
                            slot.set(dropped);
                            CompoundTag newTag = new CompoundTag();
                            newTag.put("BlockEntityTag", handlerAndTe.getSecond().saveWithoutMetadata());
                            containerStack.setTag(newTag);
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static boolean addToContainerItem(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
        if (slot.mayPickup(player)) {

            var handlerAndTe = getItemHandler(containerStack, player);
            if (handlerAndTe != null) {
                IItemHandler handler = handlerAndTe.getFirst();
                ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
                boolean success = result.isEmpty() || result.getCount() != stack.getCount();
                if (success) {
                    if (simulate) {
                        return true;
                    } else {
                        //this is a mess and probably not even correct
                        CompoundTag newTag = new CompoundTag();
                        newTag.put("BlockEntityTag", handlerAndTe.getSecond().saveWithoutMetadata());
                        if (inSlot) {
                            stack.setCount(result.getCount());
                            ItemStack newStack = containerStack.copy();
                            if (slot.mayPlace(newStack)) {
                                newStack.setTag(newTag);
                                slot.set(newStack);
                                return true;
                            }
                        } else {
                            int i = stack.getCount() - result.getCount();
                            slot.safeTake( i, i, player);
                            containerStack.setTag(newTag);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static Pair<IItemHandler, BlockEntity> getItemHandler(ItemStack containerStack, Player player) {
        CompoundTag tag = containerStack.getTag();
        if(tag != null) {
            CompoundTag cmp = tag.getCompound("BlockEntityTag");
            if (!cmp.contains("LootTable")) {
                BlockEntity te = ItemsUtil.loadBlockEntityFromItem(cmp.copy(), containerStack.getItem());

                if (te != null) {
                    if (te instanceof SafeBlockTile safe && !safe.canPlayerOpen(player, false)) return null;
                    LazyOptional<IItemHandler> handlerHolder = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    if (handlerHolder.isPresent()) {
                        return Pair.of(handlerHolder.orElseGet(EmptyHandler::new), te);
                    }
                }
            }
        }
        return null;
    }

    public static int getAllSacksInInventory(ItemStack stack, Entity entityIn, ServerPlayer player, int amount) {
        AtomicReference<IItemHandler> reference = new AtomicReference<>();
        entityIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(reference::set);
        if (reference.get() != null) {
            for (int _idx = 0; _idx < reference.get().getSlots(); _idx++) {
                ItemStack slotItem = reference.get().getStackInSlot(_idx);
                if (slotItem.getItem() instanceof SackItem) {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && tag.contains("BlockEntityTag")) {
                        amount++;
                    }
                }
            }

            if (CompatHandler.quark) {
                ItemStack backpack = player.getItemBySlot(EquipmentSlot.CHEST);
                amount += CompatHandler.getSacksInBackpack(backpack);
            }
        }
        return amount;
    }

    public static KeyLockableTile.KeyStatus hasKeyInInventory(Player player, String key) {
        if (key == null) return KeyLockableTile.KeyStatus.CORRECT_KEY;
        KeyLockableTile.KeyStatus found = KeyLockableTile.KeyStatus.INCORRECT_KEY;
        if (CompatHandler.curios) {
            found = CompatHandler.isKeyInCurio(player, key);
            if (found == KeyLockableTile.KeyStatus.CORRECT_KEY) return found;
        }

        AtomicReference<IItemHandler> itemHandler = new AtomicReference<>();
        player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(itemHandler::set);
        if (itemHandler.get() != null) {
            for (int _idx = 0; _idx < itemHandler.get().getSlots(); _idx++) {
                ItemStack stack = itemHandler.get().getStackInSlot(_idx);
                if (stack.is(ModTags.KEY)) {
                    found = KeyLockableTile.KeyStatus.INCORRECT_KEY;
                    if (KeyLockableTile.isCorrectKey(stack, key)) return KeyLockableTile.KeyStatus.CORRECT_KEY;
                }
            }
        }
        return found;
    }

    public static boolean faucetSpillItems(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
        if (itemHandler != null) {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack itemstack = itemHandler.getStackInSlot(slot);
                if (!itemstack.isEmpty()) {
                    ItemStack extracted = itemHandler.extractItem(slot, 1, false);
                    //empty stack means it can't extract from inventory
                    if (!extracted.isEmpty()) {
                        tile.setChanged();
                        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, extracted);
                        drop.setDeltaMovement(new Vec3(0, 0, 0));
                        level.addFreshEntity(drop);
                        float f = (level.random.nextFloat() - 0.5f) / 4f;
                        level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
}
