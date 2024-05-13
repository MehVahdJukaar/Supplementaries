package net.mehvahdjukaar.supplementaries.common.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public abstract class LunchBoxItem extends SelectableContainerItem<LunchBoxItem.Data> {

    public LunchBoxItem(Properties properties) {
        super(properties);
    }

    private static boolean canAcceptItem(ItemStack toInsert) {
        var anim = toInsert.getItem().getUseAnimation(toInsert);
        return anim == UseAnim.DRINK || anim == UseAnim.EAT;
    }

    public interface Data extends AbstractData {
        default boolean canAcceptItem(ItemStack toInsert) {
            return LunchBoxItem.canAcceptItem(toInsert);
        }

    }


}

