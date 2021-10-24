package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.block.tiles.EnhancedLanternBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
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
        matrixStackIn.mulPose(Const.rot(tile.getDirection().getOpposite()));
        matrixStackIn.mulPose(Const.XN90);
        // animation
        if (ceiling) {
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, tile.prevAngle * 1.5f, tile.angle * 1.5f)));
            matrixStackIn.translate(-0.5, -0.5625, -0.5);
        } else {
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, tile.prevAngle, tile.angle)));
            matrixStackIn.translate(-0.5, -0.75, -0.375);
        }
        // render block

        RendererUtil.renderBlockModel(state, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.popPose();
    }


    @Override
    public void render(T tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        BlockState state = tile.getBlockState().getBlock().defaultBlockState();

        this.renderLantern(tile, state, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
    }
}