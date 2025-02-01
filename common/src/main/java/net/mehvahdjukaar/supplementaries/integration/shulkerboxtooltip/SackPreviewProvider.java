package net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;

public final class SackPreviewProvider extends BlockEntityPreviewProvider {

    public SackPreviewProvider() {
        super(SackBlockTile.getUnlockedSlots(), true);
    }

    @Override
    public int getInventoryMaxSize(PreviewContext context) {
        return SackBlockTile.getUnlockedSlots();
    }

}
