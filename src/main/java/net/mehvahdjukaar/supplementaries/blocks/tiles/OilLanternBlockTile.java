package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.OilLanternBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.state.properties.AttachFace;

public class OilLanternBlockTile extends SwayingBlockTile{
    public OilLanternBlockTile() {
        super(Registry.COPPER_LANTERN_TILE);
    }

    @Override
    public void tick() {
        if(this.getBlockState().get(OilLanternBlock.FACE)!=AttachFace.FLOOR)
            super.tick();
    }
}
