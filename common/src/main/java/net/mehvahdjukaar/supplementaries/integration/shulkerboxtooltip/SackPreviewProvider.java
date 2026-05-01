package net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;

public final class SackPreviewProvider extends BlockEntityPreviewProvider {

    @ClientOnly
    private static PreviewRenderer renderer;

    public SackPreviewProvider() {
        super(SackBlockTile.getUnlockedSlots(), true);
    }

    @Override
    public int getInventoryMaxSize(PreviewContext context) {
        return SackBlockTile.getUnlockedSlots();
    }

    @Override
    @ClientOnly
    public PreviewRenderer getRenderer() {
        if (renderer == null) {
            renderer = new VariableSizePreviewRenderer(SackBlockTile::getUnlockedSlots, ModTextures.SACK_GUI_TEXTURE);
        }
        return renderer;
    }

}
