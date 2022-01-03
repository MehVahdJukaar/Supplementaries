package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;


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
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90 + Mth.lerp(partialTicks, tile.prevYaw, tile.yaw)));
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        RendererUtil.renderBlockModel(Materials.WIND_VANE_BLOCK_MODEL, matrixStackIn, bufferIn, blockRenderer,
                combinedLightIn, combinedOverlayIn, true);

        matrixStackIn.popPose();

    }
}