package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SpiderSkullBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpiderWallSkullBlock extends WallSkullBlock {

    public SpiderWallSkullBlock(Properties properties) {
        super(SpiderSkullBlock. TYPE, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiderSkullBlockTile( pos, state);
    }
}
