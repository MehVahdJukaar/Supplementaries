package net.mehvahdjukaar.supplementaries.common.items.fabric;

import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QuiverItemImpl {

    public static ItemStack getQuiver(LivingEntity entity) {
        if (!(entity instanceof Player) && entity instanceof IQuiverEntity e) return e.getQuiver();
        if (entity instanceof Player p) {
            for (var s : p.getInventory().items) {
                if (s.getItem() == ModRegistry.QUIVER_ITEM.get()) return s;
            }
        }
        return ItemStack.EMPTY;
    }

    public static QuiverItem.Data getQuiverData(ItemStack stack) {
        return new QuiverNBTData(stack.getOrCreateTag());
    }

    public static class QuiverNBTData implements QuiverItem.Data {

        public static final String TAG_ITEMS = "Items";
        private final List<ItemStack> stackView = new ArrayList<>(
                Collections.nCopies(CommonConfigs.Items.QUIVER_SLOTS.get(), ItemStack.EMPTY));
        private final CompoundTag tag;

        public QuiverNBTData(CompoundTag tag) {
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

        @Override
        public ItemStack tryAdding(ItemStack toInsert, boolean onlyOnExisting) {
            if (toInsert.isEmpty() || !toInsert.getItem().canFitInsideContainerItems()) {
                return toInsert;
            }
            if (!this.canAcceptItem(toInsert)) return toInsert;
            if (!tag.contains(TAG_ITEMS)) {
                tag.put(TAG_ITEMS, new ListTag());
            }
            ListTag listTag = tag.getList(TAG_ITEMS, 10);
            int ind = 0;
            for (var c : listTag) {
                if (c instanceof CompoundTag t) {
                    var st = ItemStack.of(t);
                    if (ItemStack.isSameItemSameTags(st, toInsert) && st.getCount() != st.getMaxStackSize()) {
                        int missing = st.getMaxStackSize() - st.getCount();
                        int j = Math.min(missing, toInsert.getCount());
                        toInsert.shrink(j);
                        st.grow(j);
                        st.save(t);
                        return toInsert;
                    } else if (st.isEmpty()) {
                        listTag.set(ind, toInsert.save(new CompoundTag()));
                        return ItemStack.EMPTY;
                    }
                }
                ind++;
            }
            if (listTag.size() < stackView.size()) {
                listTag.add(toInsert.save(new CompoundTag()));
                return ItemStack.EMPTY;
            }
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
        public void consumeArrow() {
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
    }
}
