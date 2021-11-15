package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.tileentity.TileEntityType;

public class OilLanternBlockTile extends EnhancedLanternBlockTile{
    public OilLanternBlockTile() {
        super(ModRegistry.COPPER_LANTERN_TILE.get());
    }

    public OilLanternBlockTile(TileEntityType type) {
        super(type);
    }

    @Override
    public void tick() {
        if(this.level.isClientSide){
            if(this.getBlockState().getValue(CopperLanternBlock.FACE)!=AttachFace.FLOOR)
                super.tick();
        }
    }
    
    @Override
    public boolean hasAnimation() {
        return this.oldRendererState;
    }
}
