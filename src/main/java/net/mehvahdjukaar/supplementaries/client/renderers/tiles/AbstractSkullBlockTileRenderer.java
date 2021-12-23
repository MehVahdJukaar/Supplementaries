package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EnhancedSkullBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public abstract class AbstractSkullBlockTileRenderer<T extends EnhancedSkullBlockTile> implements BlockEntityRenderer<T> {

    protected final Map<SkullBlock.Type, SkullModelBase> modelByType;
    protected final BlockRenderDispatcher blockRenderer;
    protected final SkullModelBase overlay;

    public AbstractSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.modelByType = SkullBlockRenderer.createSkullRenderers(context.getModelSet());
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.overlay = new SkullModel(context.bakeLayer(ClientRegistry.SKULL_CANDLE_OVERLAY));
    }

    @Override
    public void render(T tile, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, int pCombinedOverlay) {

        BlockState blockstate = tile.getBlockState();

        float yaw = 22.5F * (float) blockstate.getValue(SkullBlock.ROTATION);
        renderSkull(tile, poseStack, buffer, pCombinedLight, yaw);

    }

    public void renderSkull(T tile, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, float yaw) {
        SkullBlock.Type type = tile.getSkullType();
        SkullModelBase modelBase = this.modelByType.get(type);
        RenderType renderType = SkullBlockRenderer.getRenderType(type, tile.getOwnerProfile());
        SkullBlockRenderer.renderSkull(null, yaw, 0, poseStack, buffer, pCombinedLight, modelBase, renderType);
    }


    public void renderOverlay(PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, ResourceLocation texture, float yaw) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.25, 0.5);

        float s = 1.077f;
        poseStack.scale(-s, -s, s);
        poseStack.translate(0, 0.25f, 0);

        RenderType overlayTexture = RenderType.entityCutoutNoCullZOffset(texture);
        VertexConsumer vertexconsumer = buffer.getBuffer(overlayTexture);
        this.overlay.setupAnim(0, yaw, 0.0F);
        this.overlay.renderToBuffer(poseStack, vertexconsumer, pCombinedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }


}