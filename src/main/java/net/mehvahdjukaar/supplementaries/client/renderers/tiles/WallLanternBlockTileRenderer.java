package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;


public class WallLanternBlockTileRenderer extends EnhancedLanternBlockTileRenderer<WallLanternBlockTile> {
    public WallLanternBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(WallLanternBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if(tile.shouldRenderFancy()) {
            this.renderLantern(tile, tile.mimic, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
        }

        LOD lod = new LOD(this.renderer,tile.getBlockPos());

        tile.setFancyRenderer(lod.isNear());

    }
}