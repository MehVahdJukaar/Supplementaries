package net.mehvahdjukaar.supplementaries.integration;

import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExposureCompat {

    public static boolean isPictureItem(Item item) {
        return item instanceof PhotographItem || item instanceof StackedPhotographsItem || item instanceof AlbumItem;
    }

    public static int getMaxPictureCount(ItemStack itemstack) {
        Item item = itemstack.getItem();
        if (item instanceof StackedPhotographsItem spi) {
            return spi.getPhotographs(itemstack).size();
        } else if (item instanceof AlbumItem ai) {
            return ai.getPhotographsCount(itemstack);
        }
        return 1;
    }
}
