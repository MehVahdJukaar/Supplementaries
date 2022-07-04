package net.mehvahdjukaar.supplementaries.common.block.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public interface IColored {

    @Nullable
    DyeColor getColor();

    default boolean setColor(DyeColor color) {
        return false;
    }

    @Nullable
    default Map<DyeColor, Supplier<? extends ItemLike>> getItemColorMap() {
        return null;
    }

    default boolean supportsBlankColor() {
        return false;
    }
}
