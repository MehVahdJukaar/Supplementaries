package net.mehvahdjukaar.supplementaries.common.items.forge;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class QuiverItemImpl {

    public static ItemStack add(ItemStack quiverStack, ItemStack toInsert) {
        if (!toInsert.isEmpty() && toInsert.getItem().canFitInsideContainerItems()) {
            QuiverCapability cap = getQuiverItemStackHandler(quiverStack);
            if (cap != null) {
                return ItemHandlerHelper.insertItem(cap, toInsert, false);
            }
        }
        return ItemStack.EMPTY;
    }


    public static Optional<ItemStack> removeOne(ItemStack itemStack) {
        var h = getQuiverItemStackHandler(itemStack);
        if (h != null) {
            int i = 0;
            for (var s : h.getItems()) {
                if (!s.isEmpty()) {
                    return Optional.of(h.extractItem(i, s.getCount(), false));
                }
                i++;
            }
        }
        return Optional.empty();
    }


    public static Stream<ItemStack> getContents(ItemStack itemStack) {
        var h = getQuiverItemStackHandler(itemStack);
        if (h != null) {
            return h.getItems().stream();
        }
        return Stream.empty();
    }

    public static ItemStack getSelectedArrow(ItemStack itemStack, @Nullable Predicate<ItemStack> supporterArrows) {
        var h = getQuiverItemStackHandler(itemStack);
        if (h != null) {
            return h.getSelected(supporterArrows);
        }
        return ItemStack.EMPTY;
    }


    public static int getSelectedArrowCount(ItemStack itemStack) {
        var h = getQuiverItemStackHandler(itemStack);
        if (h != null) {
            return h.getSelectedAmount();
        }
        return 0;
    }

    @Nullable
    public static QuiverItem.QuiverTooltip getQuiverTooltip(ItemStack itemStack) {
        var h = getQuiverItemStackHandler(itemStack);
        if (h != null) {
            NonNullList<ItemStack> list = NonNullList.create();
            boolean isEmpty = true;
            for (var v : h.getItems()) {
                if (!v.isEmpty()) isEmpty = false;
                list.add(v);
            }
            if (isEmpty) return null;
            return new QuiverItem.QuiverTooltip(list, h.selectedSlot);
        }
        return null;
    }

    public static void cycleArrow(ItemStack stack) {
        var h = getQuiverItemStackHandler(stack);
        if (h != null) {
            h.cycle();
        }
    }

    public static ItemStack getQuiver(LivingEntity entity) {
        var cap = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (cap != null) {
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack quiver = cap.getStackInSlot(i);
                if (quiver.getItem() == ModRegistry.QUIVER_ITEM.get()) return quiver;
            }
        }
        return ItemStack.EMPTY;
    }

    @Contract
    @Nullable
    public static QuiverCapability getQuiverItemStackHandler(ItemStack stack) {
        return (QuiverCapability) stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }


    public static class QuiverCapability extends ItemStackHandler implements ICapabilitySerializable<CompoundTag> {

        private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(() -> this);

        //Provider
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, lazyOptional);
        }

        //actual cap

        private int selectedSlot = 0;

        public QuiverCapability() {
            super(QuiverItem.SLOTS);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof ArrowItem;
        }

        public List<ItemStack> getItems() {
            return this.stacks;
        }

        public void setSelectedSlot(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        public ItemStack getSelected(@Nullable Predicate<ItemStack> supporterArrows) {
            if (supporterArrows == null) return this.getStackInSlot(this.selectedSlot);
            int size = this.getSlots();
            for (int i = 0; i < size; i++) {
                ItemStack s = this.getStackInSlot((i + this.selectedSlot) % size);
                if (supporterArrows.test(s)) return s;
            }
            return ItemStack.EMPTY;
        }

        public int getSelectedAmount() {
            ItemStack selected = this.getSelected(null);
            int amount = 0;
            for (var item : this.getItems()) {
                if (ItemHandlerHelper.canItemStacksStack(selected, item)) {
                    amount += item.getCount();
                }
            }
            return amount;
        }

        public void cycle() {
            for (int i = 0; i < QuiverItem.SLOTS; i++) {
                this.selectedSlot = (this.selectedSlot + 1) % QuiverItem.SLOTS;
                var stack = this.getStackInSlot(selectedSlot);
                if (!stack.isEmpty()) return;
            }
        }
    }
}
