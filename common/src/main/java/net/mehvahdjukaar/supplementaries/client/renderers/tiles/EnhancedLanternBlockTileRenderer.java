package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EnhancedLanternBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;


public class EnhancedLanternBlockTileRenderer<T extends EnhancedLanternBlockTile> implements BlockEntityRenderer<T> {
    protected final BlockRenderDispatcher blockRenderer;

    public EnhancedLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    public void renderLantern(T tile, BlockState state, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn,
                              int combinedLightIn, int combinedOverlayIn, boolean ceiling) {
        matrixStackIn.pushPose();
        // rotate towards direction
        matrixStackIn.translate(0.5, 0.875, 0.5);
        matrixStackIn.mulPose(RotHlpr.rot(tile.getBlockState().getValue(WallLanternBlock.FACING).getOpposite()));
        matrixStackIn.mulPose(RotHlpr.XN90);

        float angle = tile.getSwingAngle(partialTicks);

        // animation
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(angle));
        matrixStackIn.translate(-0.5, -0.75 - tile.attachmentOffset, -0.375);

        // render block
        RenderUtil.renderBlock(0, matrixStackIn, bufferIn, state, tile.getLevel(), tile.getBlockPos(), blockRenderer);

        matrixStackIn.popPose();
    }


    @Override
    public void render(T tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        BlockState state = tile.getBlockState().getBlock().defaultBlockState();

        this.renderLantern(tile, state, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
    }
}