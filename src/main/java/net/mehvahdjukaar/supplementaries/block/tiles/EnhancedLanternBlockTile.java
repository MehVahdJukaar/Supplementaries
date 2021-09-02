package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.tileentity.TileEntityType;

public class EnhancedLanternBlockTile extends SwayingBlockTile{
    public EnhancedLanternBlockTile() {
        super(ModRegistry.COPPER_LANTERN_TILE.get());
    }

    public EnhancedLanternBlockTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 1.9f;
        maxPeriod = 28f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

}
