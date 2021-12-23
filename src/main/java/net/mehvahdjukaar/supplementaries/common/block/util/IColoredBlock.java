package net.mehvahdjukaar.supplementaries.common.block.util;

import net.minecraft.world.item.DyeColor;

public interface IColoredBlock {

    DyeColor getColor();

    default boolean setColor(DyeColor color){
        return false;
    };
}
