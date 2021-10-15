package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class OilLanternBlockTile extends EnhancedLanternBlockTile{
    public OilLanternBlockTile() {
        super(ModRegistry.COPPER_LANTERN_TILE.get());
    }

    public OilLanternBlockTile(BlockEntityType type) {
        super(type);
    }

    @Override
    public void tick() {
        if(this.level.isClientSide){
            if(this.getBlockState().getValue(CopperLanternBlock.FACE)!=AttachFace.FLOOR)
                super.tick();
        }
    }
}
