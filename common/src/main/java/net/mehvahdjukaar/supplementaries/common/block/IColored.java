package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

//TODO: this is shit, improve
public interface IColored {

    @Nullable
    DyeColor getColor();

    default boolean setColor(DyeColor color) {
        return false;
    }

    @Nullable <T extends ItemLike> Map<DyeColor, Supplier<T>> getItemColorMap();

    default boolean supportsBlankColor() {
        return false;
    }

    //casts the given object to this interface if it or its block are colored
    static Optional<IColored> getOptional(ItemLike itemLike) {
        if (itemLike instanceof IColored col) return Optional.of(col);
        if (itemLike instanceof BlockItem bi && bi.getBlock() instanceof IColored col) return Optional.of(col);
        return Optional.empty();
    }
}
