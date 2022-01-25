package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IColored {

    @Nullable
    DyeColor getColor();

    default boolean setColor(DyeColor color) {
        return false;
    }

    ;

    @Nullable
    default Map<DyeColor, RegistryObject<Item>> getItemColorMap() {
        return null;
    }

    default boolean supportsBlankColor() {
        return false;
    }
}
