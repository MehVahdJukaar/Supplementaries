package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.impl.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class UrnBlockTile extends ItemDisplayTile {

    public UrnBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.URN_TILE.get(), pos, state);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.urn");
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }
}

