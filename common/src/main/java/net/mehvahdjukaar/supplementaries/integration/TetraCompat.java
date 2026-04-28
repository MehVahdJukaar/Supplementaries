package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;

public class TetraCompat {

    @Contract
    @PlatformImpl
    public static boolean isTetraSword(Item i) {
        throw new AssertionError();
    }

    @Contract
    @PlatformImpl
    public static boolean isTetraTool(Item i) {
        throw new AssertionError();
    }
}
