package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import net.minecraft.block.AbstractBlock.Properties;

public abstract class ComplexTileBlock extends Block {
    public ComplexTileBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


}
