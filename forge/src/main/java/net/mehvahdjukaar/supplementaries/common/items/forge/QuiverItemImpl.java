package net.mehvahdjukaar.supplementaries.common.items.forge;

import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class QuiverItemImpl {

    public static ItemStack getQuiver(LivingEntity entity) {
        if (entity instanceof Player player) {
            var curioQuiver = CompatHandler.getQuiverFromModsSlots(player);
            if (!curioQuiver.isEmpty()) return curioQuiver;
            if (CommonConfigs.Tools.QUIVER_CURIO_ONLY.get()) return ItemStack.EMPTY;
        } else if (entity instanceof IQuiverEntity e) {
            return e.supplementaries$getQuiver();
        }

        var cap = CapabilityHandler.get(entity, ForgeCapabilities.ITEM_HANDLER);
        if (cap != null) {
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack quiver = cap.getStackInSlot(i);
                if (quiver.getItem() == ModRegistry.QUIVER_ITEM.get()) return quiver;
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public static QuiverItem.Data getQuiverData(ItemStack stack) {
        return CapabilityHandler.get(stack, CapabilityHandler.QUIVER_ITEM_HANDLER);
    }

    public static class QuiverCapability extends ItemStackHandler implements ICapabilitySerializable<CompoundTag>, QuiverItem.Data {

        private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(() -> this);
        private final LazyOptional<QuiverCapability> lazyOptional2 = LazyOptional.of(() -> this);

        //Provider
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
            var v = ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, lazyOptional);
            if (v.isPresent()) return v;
            v = CapabilityHandler.QUIVER_ITEM_HANDLER.orEmpty(cap, lazyOptional2);
            return v;
        }

        @Override
        public CompoundTag serializeNBT() {
            var c = super.serializeNBT();
            c.putInt("SelectedSlot", this.selectedSlot);
            return c;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            super.deserializeNBT(nbt);
            this.selectedSlot = nbt.getByte("SelectedSlot");
        }

        //actual cap

        private int selectedSlot = 0;

        public QuiverCapability() {
            super(CommonConfigs.Tools.QUIVER_SLOTS.get());
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return this.canAcceptItem(stack);
        }

        public List<ItemStack> getContentView() {
            return this.stacks;
        }

        @Override
        public int getSelectedSlot() {
            return selectedSlot;
        }

        public void setSelectedSlot(int selectedSlot) {
            if (!stacks.get(selectedSlot).isEmpty()) {
                this.selectedSlot = selectedSlot;
            }
        }

        public boolean cycle(int slotsMoved) {
            int originalSlot = this.selectedSlot;
            int maxSlots = this.stacks.size();
            slotsMoved = slotsMoved % maxSlots;
            this.selectedSlot = (maxSlots + (this.selectedSlot + slotsMoved)) % maxSlots;
            for (int i = 0; i < maxSlots; i++) {
                var stack = this.getStackInSlot(selectedSlot);
                if (!stack.isEmpty()) break;
                this.selectedSlot = (maxSlots + (this.selectedSlot + (slotsMoved >= 0 ? 1 : -1))) % maxSlots;
            }
            return originalSlot != selectedSlot;
        }

        public ItemStack tryAdding(ItemStack toInsert, boolean onlyOnExisting) {
            if (!toInsert.isEmpty() && toInsert.getItem().canFitInsideContainerItems()) {
                if (onlyOnExisting) {
                    int countToAdd = toInsert.getCount();
                    for (int i = 0; i < this.getSlots() && countToAdd > 0; i++) {
                        ItemStack s = this.getStackInSlot(i);
                        if (ItemStack.isSameItemSameTags(s, toInsert)) {
                            int newCount = Math.min(s.getMaxStackSize(), s.getCount() + countToAdd);
                            int increment = newCount - s.getCount();
                            countToAdd -= increment;
                            s.grow(increment);
                            this.onContentsChanged(i);
                        }
                    }
                    toInsert.setCount(countToAdd);
                    return toInsert;
                } else {
                    return ItemHandlerHelper.insertItem(this, toInsert, false);
                }
            }
            return toInsert;
        }

        public Optional<ItemStack> removeOneStack() {
            int i = 0;
            for (var s : this.getContentView()) {
                if (!s.isEmpty()) {
                    var extracted = this.extractItem(i, s.getCount(), false);
                    this.updateSelectedIfNeeded();
                    return Optional.of(extracted);
                }
                i++;
            }
            return Optional.empty();
        }

        @Override
        public void consumeArrow() {
            var s = this.getSelected();
            s.shrink(1);
            if (s.isEmpty()) this.stacks.set(this.selectedSlot, ItemStack.EMPTY);
            this.updateSelectedIfNeeded();
            //not implemented because it isn't needed
        }
    }
}
