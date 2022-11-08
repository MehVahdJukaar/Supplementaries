package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;


public class WindVaneBlockTileRenderer implements BlockEntityRenderer<WindVaneBlockTile> {

    private final BlockRenderDispatcher blockRenderer;

    public WindVaneBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(WindVaneBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.getYaw(partialTicks)));
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        RenderUtil.renderBlockModel(ClientRegistry.WIND_VANE_BLOCK_MODEL, matrixStackIn, bufferIn, blockRenderer,
                combinedLightIn, combinedOverlayIn, true);

        matrixStackIn.popPose();

    }
}