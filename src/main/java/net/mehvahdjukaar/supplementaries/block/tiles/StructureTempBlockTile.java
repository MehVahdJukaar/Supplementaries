package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class StructureTempBlockTile extends TileEntity implements ITickableTileEntity {
    public StructureTempBlockTile() {
        super(ModRegistry.STRUCTURE_TEMP_TILE.get());
    }

    @Override
    public void tick() {
        if (this.level.getBlockState(this.worldPosition).getBlock() == ModRegistry.STRUCTURE_TEMP.get())
            this.level.removeBlock(this.worldPosition, false);
    }
}
