package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpiderSkullBlockTile extends SkullBlockEntity {
    public SpiderSkullBlockTile(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.SPIDER_SKULL_TILE.get();
    }
}
