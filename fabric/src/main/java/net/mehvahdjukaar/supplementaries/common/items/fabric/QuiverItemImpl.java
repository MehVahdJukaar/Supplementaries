package net.mehvahdjukaar.supplementaries.common.items.fabric;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
                CompoundTag compoundtag1 = listtag.getCompound(0);
                ItemStack itemstack = ItemStack.of(compoundtag1);
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

    public static ItemStack getSelectedArrow(ItemStack itemStack, @Nullable Predicate<ItemStack> supporterArrows) {
        return ItemStack.EMPTY;
    }

    public static int getSelectedArrowCount(ItemStack pStack) {
        return 0;
    }

    public static QuiverItem.QuiverTooltip getQuiverTooltip(ItemStack pStack) {
        return null;
    }

    public static void cycleArrow(ItemStack stack) {
    }

    public static ItemStack getQuiver(LivingEntity entity) {
        return ItemStack.EMPTY;
    }

    public static QuiverItem.@Nullable IQuiverData getQuiverData(ItemStack stack) {
        return null;
    }

    public static void toggleQuiverGUI(boolean on) {
    }
}
