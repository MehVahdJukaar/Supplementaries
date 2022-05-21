package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.world.level.block.state.BlockState;

public class SilverTrapdoorBlock extends GoldTrapdoorBlock {
    public SilverTrapdoorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeOpened(BlockState state) {
        return state.getValue(POWERED);
    }
}
