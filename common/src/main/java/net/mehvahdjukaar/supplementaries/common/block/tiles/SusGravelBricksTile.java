package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SusGravelBricksTile extends BlockEntity {
    public SusGravelBricksTile( BlockPos pos, BlockState blockState) {
        super(ModRegistry.SUS_GRAVEL_BRICKS_TILE.get(), pos, blockState);
    }
}
