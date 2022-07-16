package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;

public class CCCompat {

    @ExpectPlatform
    public static boolean checkForPrintedBook(Item item) {
        throw new AssertionError();
    }

    public static void initialize() {
    }
}
