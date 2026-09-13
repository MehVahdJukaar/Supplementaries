package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PignataBlockTile extends BlockEntity {

    public PignataBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PIGNATA_TILE.get(), pos, state);
    }
}
