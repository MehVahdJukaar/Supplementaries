package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class BubbleBlockItemRenderer extends ItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int light, int combinedOverlayIn) {

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        Minecraft minecraft = Minecraft.getInstance();

        VertexUtils.renderBubble(buffer.getBuffer(Sheets.translucentCullBlockSheet()), poseStack, light,
                 BlockPos.ZERO, minecraft.level, minecraft.getFrameTime());

        poseStack.popPose();
    }
}