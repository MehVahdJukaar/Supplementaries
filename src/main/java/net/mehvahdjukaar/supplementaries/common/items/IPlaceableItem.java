package net.mehvahdjukaar.supplementaries.common.items;

import net.minecraft.world.level.block.Block;

public interface IPlaceableItem {

    Block getPlaceableBlock();

    void makePlaceable(Block b);

    default BlockPlacerItem getPlacer(){
        return BlockPlacerItem.getInstance();
    }
}
