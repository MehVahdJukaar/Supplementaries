package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.ClientSpecialModelsManager;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.ShimmerCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;


public class EnhancedLanternBlockTileRenderer<T extends WallLanternBlockTile> implements BlockEntityRenderer<T> {
    protected final BlockRenderDispatcher blockRenderer;

    public EnhancedLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    public void renderLantern(T tile, BlockState lanternState, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                              int combinedLightIn, int combinedOverlayIn, boolean ceiling) {
        poseStack.pushPose();
        // rotate towards direction
        poseStack.translate(0.5, 0.875, 0.5);
        poseStack.mulPose(RotHlpr.rot(tile.getBlockState().getValue(WallLanternBlock.FACING)));

        float angle = tile.animation.getAngle(partialTicks);

        // animation
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.75 - tile.getAttachmentOffset(), -0.375);

        BakedModel model = ClientSpecialModelsManager.getWallLanternModel(
                blockRenderer.getBlockModelShaper(), lanternState);
        // render block
        if (CompatHandler.SHIMMER) {
            ShimmerCompat.renderWithBloom(poseStack, (p, b) ->
                    RenderUtil.renderBlock(model, 0, p, b, lanternState, tile.getLevel(), tile.getBlockPos(), blockRenderer));
        } else {
            RenderUtil.renderBlock(model, 0, poseStack, bufferIn, lanternState, tile.getLevel(), tile.getBlockPos(), blockRenderer);
        }

        poseStack.popPose();
    }


    @Override
    public void render(T tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        BlockState state = tile.getBlockState().getBlock().defaultBlockState();

        this.renderLantern(tile, state, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
    }
}