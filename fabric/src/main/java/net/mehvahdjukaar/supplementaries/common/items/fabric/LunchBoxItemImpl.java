package net.mehvahdjukaar.supplementaries.common.items.fabric;

import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LunchBoxItemImpl {

    @Nullable
    public static LunchBoxItem.Data getLunchBoxData(ItemStack stack) {
        if (stack.getItem() instanceof LunchBoxItem) {
            return new LunchBoxItemImpl.LunchBoxNBTData(stack.getOrCreateTag());
        }
        return null;
    }

    public static LunchBoxItem.Data getLunchBoxDataOrThrow(ItemStack stack) {
        if (stack.getItem() instanceof LunchBoxItem) {
            return new LunchBoxItemImpl.LunchBoxNBTData(stack.getOrCreateTag());
        }
        throw new IllegalStateException("Failed to get Lunch Box data for " + stack);
    }

    public static class LunchBoxNBTData implements LunchBoxItem.Data {

        public static final String TAG_ITEMS = "Items";
        private final List<ItemStack> stackView = new ArrayList<>(
                Collections.nCopies(ModRegistry.QUIVER_ITEM.get().getMaxSlots(), ItemStack.EMPTY));
        private final CompoundTag tag;

        public LunchBoxNBTData(CompoundTag tag) {
            this.tag = tag;
        }

        @Override
        public int getSelectedSlot() {
            if (!tag.contains("SelectedSlot")) {
                setSelectedSlot(0);
            }
            return tag.getInt("SelectedSlot");
        }

        @Override
        public void setSelectedSlot(int selectedSlot) {
            this.tag.putInt("SelectedSlot", selectedSlot);
        }

        @Override
        public List<ItemStack> getContentView() {
            ListTag listTag = tag.getList(TAG_ITEMS, 10);
            for (int i = 0; i < listTag.size() && i < stackView.size(); i++) {
                stackView.set(i, ItemStack.of((CompoundTag) listTag.get(i)));
            }
            return stackView;
        }
        // try adding. returns remainder
        @Override
        public ItemStack tryAdding(ItemStack toInsert, boolean onlyOnExisting) {
            if (toInsert.isEmpty() || !toInsert.getItem().canFitInsideContainerItems()) {
                return toInsert;
            }
            if (!this.canAcceptItem(toInsert)) return toInsert;

            // Ensure the items list exists
            if (!tag.contains(TAG_ITEMS)) {
                tag.put(TAG_ITEMS, new ListTag());
            }
            ListTag listTag = tag.getList(TAG_ITEMS, 10); // 10 = CompoundTag

            // PHASE 1: merge into existing partial stacks of the same item (and same tags)
            for (int i = 0; i < listTag.size() && !toInsert.isEmpty(); i++) {
                Tag raw = listTag.get(i);
                if (!(raw instanceof CompoundTag t)) continue;

                ItemStack st = ItemStack.of(t);
                if (st.isEmpty()) continue; // handled in Phase 2

                if (ItemStack.isSameItemSameTags(st, toInsert)) {
                    int max = st.getMaxStackSize();
                    if (st.getCount() < max) {
                        int missing = max - st.getCount();
                        int move = Math.min(missing, toInsert.getCount());
                        if (move > 0) {
                            st.grow(move);
                            toInsert.shrink(move);
                            st.save(t); // write back to the same slot
                        }
                    }
                }
            }

            // If we only merge into existing stacks, stop here.
            if (onlyOnExisting || toInsert.isEmpty()) {
                return toInsert;
            }

            // PHASE 2: fill already-present EMPTY slots (represented by empty ItemStacks)
            for (int i = 0; i < listTag.size() && !toInsert.isEmpty(); i++) {
                Tag raw = listTag.get(i);
                if (!(raw instanceof CompoundTag t)) continue;

                ItemStack st = ItemStack.of(t);
                if (!st.isEmpty()) continue;

                int move = Math.min(toInsert.getCount(), toInsert.getMaxStackSize());
                if (move <= 0) break;

                ItemStack slice = toInsert.copy();
                slice.setCount(move);
                listTag.set(i, slice.save(new CompoundTag())); // overwrite empty slot
                toInsert.shrink(move);
            }

            // PHASE 3: append new slots if capacity allows
            while (!toInsert.isEmpty() && listTag.size() < stackView.size()) {
                int move = Math.min(toInsert.getCount(), toInsert.getMaxStackSize());
                if (move <= 0) break;

                ItemStack slice = toInsert.copy();
                slice.setCount(move);
                listTag.add(slice.save(new CompoundTag()));
                toInsert.shrink(move);
            }

            // Whatever couldn't fit is returned as remainder
            return toInsert;
        }



        @Override
        public Optional<ItemStack> removeOneStack() {
            if (!tag.contains(TAG_ITEMS)) {
                return Optional.empty();
            }
            ListTag listTag = tag.getList(TAG_ITEMS, 10);
            if (listTag.isEmpty()) {
                return Optional.empty();
            }
            CompoundTag compoundTag2 = listTag.getCompound(0);
            ItemStack itemStack = ItemStack.of(compoundTag2);
            listTag.remove(0);
            if (listTag.isEmpty()) {
                tag.remove(TAG_ITEMS);
            }
            return Optional.of(itemStack);
        }

        @Override
        public void consumeSelected() {
            if (tag.contains(TAG_ITEMS)) {
                ListTag listTag = tag.getList(TAG_ITEMS, 10);
                if (!listTag.isEmpty()) {
                    int selected = this.getSelectedSlot();
                    if (selected >= listTag.size()) {
                        selected = listTag.size() - 1;
                        this.setSelectedSlot(selected);
                    }
                    ItemStack arrow = ItemStack.of((CompoundTag) listTag.get(selected));
                    if (!arrow.isEmpty()) arrow.shrink(1);
                    if (arrow.isEmpty()) {
                        arrow = ItemStack.EMPTY;
                        listTag.set(selected, arrow.save(new CompoundTag()));
                        this.updateSelectedIfNeeded();
                    } else {
                        listTag.set(selected, arrow.save(new CompoundTag()));
                    }
                }
            }
        }

        @Override
        public boolean canEatFrom() {
            return tag.getBoolean("Open");
        }

        @Override
        public void switchMode() {
            tag.putBoolean("Open", !tag.getBoolean("Open"));
        }
    }
}
