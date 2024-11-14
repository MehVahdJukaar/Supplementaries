package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexModels;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CageBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.components.MobContainerView;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import static net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer.renderFluid;


public class CageItemRenderer extends ItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        //render block
        matrixStackIn.pushPose();
        BlockItem item = ((BlockItem) stack.getItem());
        BlockState state = item.getBlock().defaultBlockState();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();
        this.renderContent(stack, transformType, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    protected void renderContent(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                                 MultiBufferSource buffer, int light, int overlay) {

        MobContainerView mobContent = stack.get(ModComponents.MOB_HOLDER_CONTENT.get());
        if (mobContent != null) {
            Holder<SoftFluid> visualFluid = mobContent.getVisualFluid();
            if (visualFluid != null) {
                SoftFluid s = visualFluid.value();
                renderFluid(9 / 12f, s.getTintColor(), 0, s.getStillTexture(),
                        poseStack, buffer, light, overlay);
            }
            int fishTexture = mobContent.getFishTexture();
            if (fishTexture >= 0) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.3125, 0.5);
                poseStack.mulPose(RotHlpr.YN45);
                poseStack.scale(1.5f, 1.5f, 1.5f);
                VertexModels.renderFish(buffer, poseStack, 0, 0, fishTexture, light);
                poseStack.popPose();
            }
            Entity e = mobContent.getVisualEntity();
            if (e != null) {
                float s = mobContent.getRenderScale();
                poseStack.pushPose();

                EntityRenderDispatcher entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(RotHlpr.Y180);
                poseStack.translate(-0.5, -0.5, -0.5);

                CageBlockTileRenderer.renderMobStatic(e, s, entityRenderer, poseStack, 1, buffer, light, -90);

                poseStack.popPose();
            }
        }
    }


}

