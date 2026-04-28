package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CCCompat {

    @PlatformImpl
    public static void setup() {
    }

    @PlatformImpl
    public static void init() {
    }

    @PlatformImpl
    public static boolean isPrintedBook(Item item) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static int getPages(ItemStack itemstack) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static String[] getText(ItemStack itemstack) {
        throw new AssertionError();
    }
}
