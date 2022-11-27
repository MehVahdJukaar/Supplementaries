package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EnhancedSkullBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public abstract class SkullWithWaxTileRenderer<T extends EnhancedSkullBlockTile> implements BlockEntityRenderer<T> {

    private final BlockEntityRenderDispatcher dispatcher;

    protected final BlockRenderDispatcher blockRenderer;
    protected final SkullModelBase overlay;

    protected SkullWithWaxTileRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.overlay = new SkullModel(context.bakeLayer(ClientRegistry.SKULL_CANDLE_OVERLAY));
        this.dispatcher = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public void render(T tile, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, int pCombinedOverlay) {
        BlockEntity inner = tile.getSkullTile();
        if (inner != null) {
            float yaw = -22.5F * (float) (tile.getBlockState().getValue(SkullBlock.ROTATION)
                    - inner.getBlockState().getValue(SkullBlock.ROTATION));
            //let's base block master the rotation


            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
            poseStack.translate(-0.5, -0.5, -0.5);


            renderInner(inner, pPartialTicks, poseStack, buffer, pCombinedLight, pCombinedOverlay);


            //blockRenderer.renderSingleBlock(blockstate, poseStack, buffer, pCombinedLight, pCombinedOverlay, ModelData.EMPTY);
        }
        //leaves the matrix rotated for wax
    }

    public <B extends BlockEntity> void renderInner(B tile, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, int pCombinedOverlay) {
        BlockEntityRenderer<B> renderer = dispatcher.getRenderer(tile);
        if (renderer != null) {
            renderer.render(tile, pPartialTicks, poseStack, buffer, pCombinedLight, pCombinedOverlay);
        }
    }

    public void renderWax(PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, @Nullable ResourceLocation texture, float yaw) {
        if (texture == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.25, 0.5);

        float s = 1.077f;
        poseStack.scale(-s, -s, s);
        poseStack.translate(0, 0.25f, 0);

        RenderType overlayTexture = RenderType.entityCutoutNoCullZOffset(texture);
        VertexConsumer vertexconsumer = buffer.getBuffer(overlayTexture);
        this.overlay.setupAnim(0, -yaw, 0.0F);
        this.overlay.renderToBuffer(poseStack, vertexconsumer, pCombinedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }


}