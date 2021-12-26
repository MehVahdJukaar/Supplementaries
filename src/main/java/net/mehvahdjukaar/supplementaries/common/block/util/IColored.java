package net.mehvahdjukaar.supplementaries.common.block.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IColored {

    @Nullable
    DyeColor getColor();

    default boolean setColor(DyeColor color){
        return false;
    };

    @Nullable
    default Map<DyeColor, RegistryObject<Item>> getItemColorMap(){
        return null;
    }

    default boolean supportsBlankColor(){
        return false;
    }
}
