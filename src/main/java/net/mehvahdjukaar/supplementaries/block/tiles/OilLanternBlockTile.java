package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.OilLanternBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.state.properties.AttachFace;

public class OilLanternBlockTile extends EnhancedLanternBlockTile{
    public OilLanternBlockTile() {
        super(Registry.COPPER_LANTERN_TILE.get());
    }

    @Override
    public void tick() {
        if(this.world.isRemote){
            if(this.getBlockState().get(OilLanternBlock.FACE)!=AttachFace.FLOOR)
                super.tick();
        }
    }
}
