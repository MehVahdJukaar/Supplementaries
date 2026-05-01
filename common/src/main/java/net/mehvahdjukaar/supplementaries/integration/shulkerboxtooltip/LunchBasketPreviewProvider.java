package net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.supplementaries.common.block.tiles.LunchBoxBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;

public final class LunchBasketPreviewProvider extends BlockEntityPreviewProvider {

    @ClientOnly
    private static PreviewRenderer renderer;

    public LunchBasketPreviewProvider() {
        super(LunchBoxBlockTile.getUnlockedSlots(), true);
    }

    @Override
    public int getInventoryMaxSize(PreviewContext context) {
        return LunchBoxBlockTile.getUnlockedSlots();
    }

    @Override
    @ClientOnly
    public PreviewRenderer getRenderer() {
        if (renderer == null) {
            renderer = new VariableSizePreviewRenderer(LunchBoxBlockTile::getUnlockedSlots, ModTextures.LUNCH_BASKET_GUI_TEXTURE);
        }
        return renderer;
    }

}
