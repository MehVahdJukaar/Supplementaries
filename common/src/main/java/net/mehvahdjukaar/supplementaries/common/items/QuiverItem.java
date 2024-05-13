package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class QuiverItem extends SelectableContainerItem<QuiverItem.Data> implements DyeableLeatherItem {

    public QuiverItem(Properties properties) {
        super(properties);
    }

    @Override
    public Data getData(ItemStack stack) {
        return getQuiverData(stack);
    }

    @Nullable
    @ExpectPlatform
    public static QuiverItem.Data getQuiverData(ItemStack stack) {
        throw new AssertionError();
    }

    @NotNull
    @ExpectPlatform
    public static ItemStack getQuiver(LivingEntity entity) {
        throw new AssertionError();
    }


    public static boolean canAcceptItem(ItemStack toInsert) {
        return toInsert.getItem() instanceof ArrowItem && !toInsert.is(ModTags.QUIVER_BLACKLIST);
    }

    //this is cap, cap provider
    public interface Data extends AbstractData {

        default boolean canAcceptItem(ItemStack toInsert) {
            return QuiverItem.canAcceptItem(toInsert);
        }

        default ItemStack getSelected() {
            return getSelected(null);
        }

        default ItemStack getSelected(@Nullable Predicate<ItemStack> supporterArrows) {
            var content = this.getContentView();
            int selected = this.getSelectedSlot();
            if (supporterArrows == null) return content.get(selected);
            int size = content.size();
            for (int i = 0; i < size; i++) {
                ItemStack s = content.get((i + selected) % size);
                if (supporterArrows.test(s)) return s;
            }
            return ItemStack.EMPTY;
        }
    }
}

