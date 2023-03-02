package net.mehvahdjukaar.supplementaries.integration.forge;


import net.minecraft.world.item.Item;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;

public class TetraCompatImpl {
    public static boolean isTetraSword(Item i) {
        return i instanceof ModularBladedItem;
    }

    public static boolean isTetraTool(Item i) {
        return (i instanceof ModularDoubleHeadedItem || i instanceof ModularSingleHeadedItem);
    }

}

