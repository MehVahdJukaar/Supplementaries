package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.StructureTempBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class StructureTempBlock extends Block {

    public StructureTempBlock(Properties properties) {
        super(properties);

    }
    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new StructureTempBlockTile();
    }

}
