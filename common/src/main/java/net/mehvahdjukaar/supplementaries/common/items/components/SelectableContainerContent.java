package net.mehvahdjukaar.supplementaries.common.items.components;

import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class SelectableContainerContent<M extends SelectableContainerContent.Mut<?>> implements TooltipComponent, TooltipProvider {

    protected final NonNullList<ItemStack> stacks;
    protected final int selectedSlot;
    protected final int selectedItemCount;
    protected final boolean empty;

    public SelectableContainerContent(List<ItemStack> stacks, int selected) {
        this.stacks = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            this.stacks.set(i, stacks.get(i));
        }
        this.selectedSlot = selected;
        this.selectedItemCount = computeSelectedItemCount(stacks, selected);
        this.empty = stacks.stream().allMatch(ItemStack::isEmpty);
    }

    public abstract M toMutable();

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getSelectedItemCount() {
        return selectedItemCount;
    }

    public List<ItemStack> getContentCopy() {
        return this.stacks.stream().map(ItemStack::copy).toList();
    }

    /**
     * Do not modify this list directly if you are on fabric. On forge it can be modified
     */
    @ApiStatus.Internal
    public List<ItemStack> getContentUnsafe() {
        return stacks;
    }

    public ItemStack getSelectedUnsafe() {
        return this.stacks.get(this.selectedSlot);
    }

    public ItemStack getSelected() {
        return this.stacks.get(this.selectedSlot).copy();
    }

    public Item getSelectedItem() {
        return this.stacks.get(this.selectedSlot).getItem();
    }

    public int getSelectedCount() {
        return this.stacks.get(this.selectedSlot).getCount();
    }

    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot, this.stacks);
        return this.stacks.get(slot).copy();
    }

    public int getSize() {
        return this.stacks.size();
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (selectedItemCount != 0) {
            tooltipAdder.accept(Component.translatable("message.supplementaries.quiver.tooltip",
                            getSelectedItem().getDescription(), selectedItemCount)
                    .withStyle(ChatFormatting.GRAY));
        }

    }

    private static int computeSelectedItemCount(List<ItemStack> stacks, int sel) {
        ItemStack selected = stacks.get(sel);
        int amount = 0;
        for (var item : stacks) {

            if (ItemStack.isSameItemSameComponents(selected, item)) {
                amount += item.getCount();
            }
        }
        return amount;
    }

    protected static void validateSlotIndex(int slot, List<?> list) {
        if (slot < 0 || slot >= list.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + list.size() + ")");
        }
    }

    public int getBarSize() {
        return Math.min(1 + 12 * selectedItemCount /
                (stacks.get(selectedSlot).getMaxStackSize() * getSize()), 13);
    }

    public boolean isEmpty() {
        return empty;
    }


    public abstract static class Mut<T extends SelectableContainerContent<?>> {

        protected final NonNullList<ItemStack> stacks;
        protected int selectedSlot;

        protected Mut(SelectableContainerContent<?> original) {
            this.stacks = NonNullList.withSize(original.stacks.size(), ItemStack.EMPTY);
            for (int i = 0; i < original.stacks.size(); i++) {
                this.stacks.set(i, original.stacks.get(i).copy());
            }
            this.selectedSlot = original.selectedSlot;
        }

        public abstract T toImmutable();


        @ForgeOverride
        public void setStackInSlot(int slot, ItemStack stack) {
            validateSlotIndex(slot, stacks);
            this.stacks.set(slot, stack);
        }

        @ForgeOverride
        public ItemStack getStackInSlot(int slot) {
            validateSlotIndex(slot, stacks);
            return this.stacks.get(slot);
        }

        public ItemStack getSelected() {
            return this.stacks.get(this.selectedSlot);
        }

        public List<ItemStack> getStacks() {
            return stacks;
        }

        @ForgeOverride
        public int getSlots() {
            return this.stacks.size();
        }

        protected void updateSelectedIfNeeded() {
            this.cycle(0); //this works
        }

        // true if success
        public boolean setSelectedSlot(int selectedSlot) {
            validateSlotIndex(selectedSlot, stacks);
            if (!stacks.get(selectedSlot).isEmpty()) {
                this.selectedSlot = selectedSlot;
                return true;
            }
            return false;
        }

        public int getSelectedSlot() {
            return selectedSlot;
        }

        @Nullable
        public ItemStack tryRemovingOne() {
            int i = 0;
            for (var s : this.stacks) {
                if (!s.isEmpty()) {
                    var extracted = this.extractItem(i, s.getCount(), false);
                    this.updateSelectedIfNeeded();
                    return extracted;
                }
                i++;
            }
            return null;
        }

        public boolean isItemValid(ItemStack stack) {
            return isItemValid(0, stack);
        }

        @ForgeOverride
        public abstract boolean isItemValid(int slot, ItemStack stack);

        /**
         * Adds one item. returns the item that is remaining and has not been added. Same item if no change was made
         */
        public ItemStack tryAdding(ItemStack toInsert, boolean onlyOnExisting) {
            if (toInsert.isEmpty()) return toInsert;
            if (onlyOnExisting) {
                int finalCount = toInsert.getCount();
                for (int i = 0; i < this.getSlots() && finalCount > 0; i++) {
                    ItemStack s = this.getStackInSlot(i);
                    if (ItemStack.isSameItemSameComponents(s, toInsert)) {
                        int newCount = Math.min(s.getMaxStackSize(), s.getCount() + finalCount);
                        int increment = newCount - s.getCount();
                        finalCount -= increment;
                        s.grow(increment);
                    }
                }
                toInsert.setCount(finalCount);
            } else {
                for (int i = 0; i < this.getSlots(); ++i) {
                    toInsert = this.insertItem(i, toInsert, false);
                    if (toInsert.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            return toInsert;
        }

        public ItemStack tryAdding(ItemStack pInsertedStack) {
            return tryAdding(pInsertedStack, false);
        }


        public boolean cycle() {
            return cycle(1);
        }

        public boolean cycle(boolean clockWise) {
            return cycle(clockWise ? 1 : -1);
        }

        public boolean cycle(int slotsMoved) {
            ItemStack selected;
            int originalSlot = this.selectedSlot;
            if (slotsMoved == 0) {
                //returns if it doesn't have to move
                selected = stacks.get(selectedSlot);
                if (!selected.isEmpty()) return false;
            }
            int maxSlots = stacks.size();
            slotsMoved = slotsMoved % maxSlots;
            this.selectedSlot = ((maxSlots + (selectedSlot + slotsMoved)) % maxSlots);
            for (int i = 0; i < maxSlots; i++) {
                selected = stacks.get(selectedSlot);
                if (!selected.isEmpty()) break;
                this.selectedSlot = ((maxSlots + (selectedSlot + (slotsMoved >= 0 ? 1 : -1))) % maxSlots);
            }
            return originalSlot != selectedSlot;
        }

        @ForgeOverride
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            } else if (!this.isItemValid(slot, stack)) {
                return stack;
            } else {
                validateSlotIndex(slot, this.stacks);
                ItemStack existing = this.stacks.get(slot);
                int limit = this.getStackLimit(slot, stack);
                if (!existing.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                        return stack;
                    }

                    limit -= existing.getCount();
                }

                if (limit <= 0) {
                    return stack;
                } else {
                    boolean reachedLimit = stack.getCount() > limit;
                    if (!simulate) {
                        if (existing.isEmpty()) {
                            this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
                        } else {
                            existing.grow(reachedLimit ? limit : stack.getCount());
                        }
                    }

                    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
                }
            }
        }

        @ForgeOverride
        public int getSlotLimit(int slot) {
            return 99;
        }

        protected int getStackLimit(int slot, ItemStack stack) {
            return Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
        }

        @ForgeOverride
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0) {
                return ItemStack.EMPTY;
            } else {
                validateSlotIndex(slot, stacks);
                ItemStack existing = this.stacks.get(slot);
                if (existing.isEmpty()) {
                    return ItemStack.EMPTY;
                } else {
                    int toExtract = Math.min(amount, existing.getMaxStackSize());
                    if (existing.getCount() <= toExtract) {
                        if (!simulate) {
                            this.stacks.set(slot, ItemStack.EMPTY);
                            return existing;
                        } else {
                            return existing.copy();
                        }
                    } else {
                        if (!simulate) {
                            this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                        }

                        return existing.copyWithCount(toExtract);
                    }
                }
            }
        }

        public void consumeSelected(int toDecrement) {
            for (int i = this.selectedSlot; i < this.selectedSlot + this.stacks.size(); i = (i + 1) % this.stacks.size()) {
                ItemStack s = this.stacks.get(i);
                if (!s.isEmpty()) {
                    int decrement = Math.min(toDecrement, s.getCount());
                    s.shrink(decrement);
                    if(s.isEmpty()){
                        this.setStackInSlot(i, ItemStack.EMPTY);
                    }
                    toDecrement -= decrement;
                    if (toDecrement <= 0) return;
                }
            }
            Supplementaries.error();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectableContainerContent<?> that = (SelectableContainerContent<?>) o;
        return selectedSlot == that.selectedSlot && selectedItemCount == that.selectedItemCount &&
                ItemStack.listMatches(stacks, that.stacks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ItemStack.hashStackList(stacks), selectedSlot);
    }
}
