package net.mehvahdjukaar.supplementaries.common.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public interface IPlaceableItem {

    void addPlaceable(Block block);

    @Nullable
    BlockItem getBlockItemOverride();
}
