package net.mehvahdjukaar.supplementaries.common.items.fabric;

import net.mehvahdjukaar.supplementaries.client.renderers.entities.QuiverLayer;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class QuiverItemImpl {

    public static int add(ItemStack pBundleStack, ItemStack pInsertedStack) {
        return 0;
    }

    public static Optional<ItemStack> removeOne(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getOrCreateTag();
        if (!compoundtag.contains("Items")) {
            return Optional.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", 10);
            if (listtag.isEmpty()) {
                return Optional.empty();
            } else {
                int i = 0;
                CompoundTag compound = listtag.getCompound(0);
                ItemStack itemstack = ItemStack.of(compound);
                listtag.remove(0);
                if (listtag.isEmpty()) {
                    pStack.removeTagKey("Items");
                }

                return Optional.of(itemstack);
            }
        }
    }

    public static Stream<ItemStack> getContents(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag == null) {
            return Stream.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", 10);
            return listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }


    public static ItemStack getQuiver(LivingEntity entity) {
        if (entity instanceof Player p) {
            for (var s : p.getInventory().items) {
                if (s.getItem() == ModRegistry.QUIVER_ITEM.get()) return s;
            }
        }
        return ItemStack.EMPTY;
    }

    public static QuiverItem.@Nullable IQuiverData getQuiverData(ItemStack stack) {
        return new QuiverNBTData();
    }

    public static class QuiverNBTData implements QuiverItem.IQuiverData {

        private int selectedSlot = 0;

        @Override
        public int getSelectedSlot() {
            return selectedSlot;
        }

        @Override
        public void setSelectedSlot(int selectedSlot) {

        }

        @Override
        public List<ItemStack> getContent() {
            return List.of(Items.DIAMOND.getDefaultInstance(), Items.EMERALD.getDefaultInstance(), Items.REDSTONE.getDefaultInstance(),
                    Items.SHEARS.getDefaultInstance(), Items.DIAMOND.getDefaultInstance(), Items.DIAMOND.getDefaultInstance());
        }

        @Override
        public ItemStack getSelected(@Nullable Predicate<ItemStack> supporterArrows) {
            return getContent().get(selectedSlot);
        }

        @Override
        public void cycle(int slotsMoved) {

        }

        @Override
        public ItemStack add(ItemStack pInsertedStack) {
            return pInsertedStack;
        }

        @Override
        public Optional<ItemStack> removeOne() {
            return Optional.empty();
        }
    }
}
