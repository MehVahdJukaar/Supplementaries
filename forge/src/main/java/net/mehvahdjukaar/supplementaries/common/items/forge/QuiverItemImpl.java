package net.mehvahdjukaar.supplementaries.common.items.forge;

import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class QuiverItemImpl {

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

    @Nullable
    public static QuiverItem.IQuiverData getQuiverData(ItemStack stack) {
        return (QuiverCapability) stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }


    public static class QuiverCapability extends ItemStackHandler implements ICapabilitySerializable<CompoundTag>, QuiverItem.IQuiverData {

        private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(() -> this);

        //Provider
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, lazyOptional);
        }

        //actual cap

        private int selectedSlot = 0;

        public QuiverCapability() {
            super(CommonConfigs.Items.QUIVER_SLOTS.get());
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof ArrowItem;
        }

        public List<ItemStack> getContent() {
            return this.stacks;
        }

        @Override
        public int getSelectedSlot() {
            return selectedSlot;
        }

        public void setSelectedSlot(int selectedSlot) {
            if(!stacks.get(selectedSlot).isEmpty()) {
                this.selectedSlot = selectedSlot;
            }
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

        public void cycle(int slotsMoved) {
            int maxSlots = this.stacks.size();
            slotsMoved = slotsMoved % maxSlots;
            this.selectedSlot = (maxSlots + (this.selectedSlot + slotsMoved)) % maxSlots;
            for (int i = 0; i < maxSlots; i++) {
                var stack = this.getStackInSlot(selectedSlot);
                if (!stack.isEmpty()) return;
                this.selectedSlot = (maxSlots + (this.selectedSlot + (slotsMoved >= 0 ? 1 : -1))) % maxSlots;
            }
        }

        public ItemStack add(ItemStack toInsert) {
            if (!toInsert.isEmpty() && toInsert.getItem().canFitInsideContainerItems()) {
                return ItemHandlerHelper.insertItem(this, toInsert, false);
            }
            return ItemStack.EMPTY;
        }

        public Optional<ItemStack> removeOne() {
            int i = 0;
            for (var s : this.getContent()) {
                if (!s.isEmpty()) {
                    var extracted = this.extractItem(i, s.getCount(), false);
                    this.updateIfNeededSelected();
                    return Optional.of(extracted);
                }
                i++;
            }
            return Optional.empty();
        }
    }
}
