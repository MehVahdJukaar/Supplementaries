package net.mehvahdjukaar.supplementaries.common.items.forge;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import java.util.stream.Stream;

public class QuiverItemImpl {
    public static ItemStack add(ItemStack quiverStack, ItemStack toInsert) {
        if (!toInsert.isEmpty() && toInsert.getItem().canFitInsideContainerItems()) {
            QuiverCapability cap = getQuiverItemStackHandler(quiverStack);
            if (cap != null) {
                ItemHandlerHelper.insertItem(cap, toInsert, false);
            }
        }
        return null;
    }


    public static Optional<ItemStack> removeOne(ItemStack pStack) {
        return null;
    }

    @Nullable
    public static QuiverCapability getQuiverItemStackHandler(ItemStack stack) {
        return (QuiverCapability) stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }

    public static Stream<ItemStack> getContents(ItemStack pStack) {
        return null;
    }

    public static class QuiverCapability extends ItemStackHandler implements ICapabilitySerializable<CompoundTag> {

        private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(() -> this);

        //Provider
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, lazyOptional);
        }

        //actual cap
        public static final int NUMBER_SLOTS = 5;

        public QuiverCapability() {
            super(NUMBER_SLOTS);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof ArrowItem;
        }

        public List<ItemStack> getItems() {
            return this.stacks.stream().toList();
        }
    }
}
