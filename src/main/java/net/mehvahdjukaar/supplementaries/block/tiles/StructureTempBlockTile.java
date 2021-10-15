package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StructureTempBlockTile extends BlockEntity implements TickableBlockEntity {
    public StructureTempBlockTile() {
        super(ModRegistry.STRUCTURE_TEMP_TILE.get());
    }

    @Override
    public void tick() {
        if(this.level.getBlockState(this.worldPosition).getBlock()== ModRegistry.STRUCTURE_TEMP.get())
        this.level.removeBlock(this.worldPosition,false);
    }
}
