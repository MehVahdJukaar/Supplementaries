package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.EndermanSkullBlockTileRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static net.mehvahdjukaar.supplementaries.client.renderers.tiles.EndermanSkullBlockTileRenderer.EYES;
import static net.mehvahdjukaar.supplementaries.client.renderers.tiles.EndermanSkullBlockTileRenderer.TEXTURE;
import static net.minecraft.client.renderer.blockentity.SkullBlockRenderer.renderSkull;


public class EndermanHeadItemRenderer extends ItemStackRenderer {


    public EndermanHeadItemRenderer() {
        super();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int combinedOverlayIn) {
        float f = 0;
        var model = EndermanSkullBlockTileRenderer.MODEL;
        if (model == null) return;
        float rot = 180;
        RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        poseStack.pushPose();

        renderSkull(null, rot, f, poseStack, bufferSource, packedLight, model, renderType);

        renderType = RenderType.eyes(EYES);
        renderSkull(null, rot, f, poseStack, bufferSource, 15728640, model, renderType);

        poseStack.popPose();
    }
}