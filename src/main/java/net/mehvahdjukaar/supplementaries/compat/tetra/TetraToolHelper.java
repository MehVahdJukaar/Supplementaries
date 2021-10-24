package net.mehvahdjukaar.supplementaries.compat.tetra;

import net.minecraft.world.item.Item;

public class TetraToolHelper {
    public static boolean isTetraSword(Item i) {
        return false;
        //return i instanceof ModularBladedItem;
    }

    public static boolean isTetraTool(Item i) {
        return false;
        //return (i instanceof ModularDoubleHeadedItem || i instanceof ModularSingleHeadedItem);
    }

}
