package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WallLanternBlockTile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;


public class WallLanternBlockTileRenderer extends EnhancedLanternBlockTileRenderer<WallLanternBlockTile> {

    private final Camera camera;

    public WallLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    }

    @Override
    public void render(WallLanternBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if(tile.shouldRenderFancy()) {
            this.renderLantern(tile, tile.getHeldBlock(), partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
        }

        LOD lod = new LOD(camera,tile.getBlockPos());

        tile.setFancyRenderer(lod.isNear());

    }
}