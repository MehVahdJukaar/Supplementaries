package net.mehvahdjukaar.supplementaries.common.items;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class ContainerItemStorage extends CombinedSlottedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> {

    private ContainerItemStorage(List<SingleSlotStorage<ItemVariant>> slots) {
        super(slots);
    }

    private static ContainerItemStorage create(int size, IntFunction<SimpleSlotStorage> slotFactory) {
        List<SingleSlotStorage<ItemVariant>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(slotFactory.apply(i));
        }
        return new ContainerItemStorage(list);
    }

    public static ContainerItemStorage ofSack(ContainerItemContext ctx) {
        return create(SackBlockTile.getUnlockedSlots(), i -> new SackStorage(ctx, i));
    }

    @Nullable
    public static ContainerItemStorage ofSelectable(ContainerItemContext ctx) {
        if (!(ctx.getItemVariant().getItem() instanceof SelectableContainerItem<?, ?> se)) return null;
        return ofSelectable(ctx, se);
    }

    @Nullable
    private static <C extends SelectableContainerContent<M>, M extends SelectableContainerContent.Mut<C>> ContainerItemStorage ofSelectable(
            ContainerItemContext ctx, SelectableContainerItem<C, M> item) {
        C content = ctx.getItemVariant().getComponentMap().get(item.getComponentType());
        if (content == null) return null;
        return create(content.getSize(), i -> new SelectableContainerStorage<>(ctx, i, item.getComponentType()));
    }

    //i hope this is right
    private abstract static class SimpleSlotStorage implements SingleSlotStorage<ItemVariant> {
        protected final ContainerItemContext ctx;
        protected final Item item;
        protected final int slot;

        protected SimpleSlotStorage(ContainerItemContext ctx, int slot) {
            this.ctx = ctx;
            this.item = ctx.getItemVariant().getItem();
            this.slot = slot;
        }

        protected abstract List<ItemStack> read(ItemStack container);

        protected abstract void write(ItemStack container, List<ItemStack> stacks);

        protected abstract boolean canInsert(ItemStack container, ItemStack stack);

        //the context item can change between calls so we always read it again
        @Nullable
        private ItemStack currentContainer() {
            ItemStack container = ctx.getItemVariant().toStack();
            return container.is(item) ? container : null;
        }

        private ItemStack getStack() {
            ItemStack container = currentContainer();
            if (container == null) return ItemStack.EMPTY;
            List<ItemStack> stacks = read(container);
            return slot < stacks.size() ? stacks.get(slot) : ItemStack.EMPTY;
        }

        private long exchangeIfMoved(ItemStack modifiedContainer, int moved, TransactionContext transaction) {
            if (moved > 0 && ctx.exchange(ItemVariant.of(modifiedContainer), 1, transaction) == 1) return moved;
            return 0;
        }

        @Override
        public long insert(ItemVariant variant, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(variant, maxAmount);
            ItemStack container = currentContainer();
            ItemStack toInsert = variant.toStack();
            if (container == null || !canInsert(container, toInsert)) return 0;

            List<ItemStack> stacks = read(container);
            if (slot >= stacks.size()) return 0;
            ItemStack current = stacks.get(slot);
            if (!current.isEmpty() && !variant.matches(current)) return 0;

            int inserted = (int) Math.min(maxAmount, toInsert.getMaxStackSize() - current.getCount());
            if (inserted <= 0) return 0;

            stacks.set(slot, toInsert.copyWithCount(current.getCount() + inserted));
            write(container, stacks);
            return exchangeIfMoved(container, inserted, transaction);
        }

        @Override
        public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(variant, maxAmount);
            ItemStack container = currentContainer();
            if (container == null) return 0;

            List<ItemStack> stacks = read(container);
            if (slot >= stacks.size()) return 0;
            ItemStack current = stacks.get(slot);
            if (!variant.matches(current)) return 0;

            int extracted = (int) Math.min(maxAmount, current.getCount());
            int left = current.getCount() - extracted;
            stacks.set(slot, left == 0 ? ItemStack.EMPTY : current.copyWithCount(left));
            write(container, stacks);
            return exchangeIfMoved(container, extracted, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return getStack().isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(getStack());
        }

        @Override
        public long getAmount() {
            return getStack().getCount();
        }

        @Override
        public long getCapacity() {
            ItemStack stack = getStack();
            return stack.isEmpty() ? Item.DEFAULT_MAX_STACK_SIZE : stack.getMaxStackSize();
        }
    }

    private static class SackStorage extends SimpleSlotStorage {
        private SackStorage(ContainerItemContext ctx, int slot) {
            super(ctx, slot);
        }

        @Override
        protected List<ItemStack> read(ItemStack container) {
            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            container.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
            return items;
        }

        @Override
        protected void write(ItemStack container, List<ItemStack> stacks) {
            container.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
        }

        @Override
        protected boolean canInsert(ItemStack container, ItemStack stack) {
            return MiscUtils.isAllowedInShulker(stack, null);
        }
    }

    private static class SelectableContainerStorage<C extends SelectableContainerContent<M>, M extends SelectableContainerContent.Mut<C>>
            extends SimpleSlotStorage {
        private final DataComponentType<C> componentType;

        private SelectableContainerStorage(ContainerItemContext ctx, int slot, DataComponentType<C> componentType) {
            super(ctx, slot);
            this.componentType = componentType;
        }

        @Override
        protected List<ItemStack> read(ItemStack container) {
            C c = container.get(componentType);
            return c != null ? c.toMutable().getStacks() : List.of();
        }

        @Override
        protected void write(ItemStack container, List<ItemStack> stacks) {
            C c = container.get(componentType);
            if (c == null) return;
            M mutable = c.toMutable();
            for (int i = 0; i < stacks.size(); i++) {
                mutable.getStacks().set(i, stacks.get(i));
            }
            container.set(componentType, mutable.toImmutable());
        }

        @Override
        protected boolean canInsert(ItemStack container, ItemStack stack) {
            C c = container.get(componentType);
            return c != null && c.toMutable().isItemValid(stack);
        }
    }
}
