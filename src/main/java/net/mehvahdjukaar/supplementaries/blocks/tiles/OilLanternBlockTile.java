package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.OilLanternBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.state.properties.AttachFace;

public class OilLanternBlockTile extends SwayingBlockTile{
    public OilLanternBlockTile() {
        super(Registry.COPPER_LANTERN_TILE);
    }

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 1.9f;
        maxPeriod = 28f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    @Override
    public void tick() {
        if(this.getBlockState().get(OilLanternBlock.FACE)!=AttachFace.FLOOR)
            super.tick();
    }
}
