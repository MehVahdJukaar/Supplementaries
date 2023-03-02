package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;

public class TetraCompat {

    @Contract
    @ExpectPlatform
    public static boolean isTetraSword(Item i) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean isTetraTool(Item i) {
        throw new AssertionError();
    }
}
