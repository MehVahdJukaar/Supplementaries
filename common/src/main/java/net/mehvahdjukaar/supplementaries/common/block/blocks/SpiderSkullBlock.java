package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SpiderSkullBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpiderSkullBlock extends SkullBlock {

    public static final SkullBlock.Type TYPE = () -> "supplementaries_spider_skull";

    public SpiderSkullBlock(Properties properties) {
        super(TYPE, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiderSkullBlockTile( pos, state);
    }
}
