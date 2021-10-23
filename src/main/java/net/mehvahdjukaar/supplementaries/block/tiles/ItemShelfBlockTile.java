package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.ItemShelfBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;

public class ItemShelfBlockTile extends ItemDisplayTile {

    public ItemShelfBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.ITEM_SHELF_TILE.get(), pos, state);
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.item_shelf");
    }

    public float getYaw() {
        return -this.getDirection().getOpposite().toYRot();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ItemShelfBlock.FACING);
    }
}

