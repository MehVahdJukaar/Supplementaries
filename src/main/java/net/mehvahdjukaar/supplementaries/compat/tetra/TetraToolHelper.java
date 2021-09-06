package net.mehvahdjukaar.supplementaries.compat.tetra;

import net.mehvahdjukaar.supplementaries.block.tiles.StatueBlockTile;
import net.minecraft.item.Item;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;

import javax.annotation.Nullable;
import java.util.Optional;

public class TetraToolHelper {
    @Nullable
    public Optional<StatueBlockTile.StatuePose> getPoseForItem(Item i) {
        if (i instanceof ModularBladedItem) return Optional.of(StatueBlockTile.StatuePose.SWORD);
        if (i instanceof ModularDoubleHeadedItem || i instanceof ModularSingleHeadedItem) return Optional.of(StatueBlockTile.StatuePose.TOOL);
        return Optional.empty();
    }

}
