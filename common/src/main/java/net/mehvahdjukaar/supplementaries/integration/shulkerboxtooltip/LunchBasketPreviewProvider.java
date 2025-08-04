package net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.supplementaries.common.block.tiles.LunchBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;

public final class LunchBasketPreviewProvider extends BlockEntityPreviewProvider {

    @Environment(EnvType.CLIENT)
    private static PreviewRenderer renderer;

    public LunchBasketPreviewProvider() {
        super(LunchBoxBlockTile.getUnlockedSlots(), true);
    }

    @Override
    public int getInventoryMaxSize(PreviewContext context) {
        return LunchBoxBlockTile.getUnlockedSlots();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public PreviewRenderer getRenderer() {
        if (renderer == null) {
            renderer = new VariableSizePreviewRenderer(LunchBoxBlockTile::getUnlockedSlots, ModTextures.LUNCH_BASKET_GUI_TEXTURE);
        }
        return renderer;
    }

}
