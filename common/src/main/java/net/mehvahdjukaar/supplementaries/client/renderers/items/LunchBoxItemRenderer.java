package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class LunchBoxItemRenderer extends ItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        //render block
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (transformType != ItemDisplayContext.GUI && player != null && player.isUsingItem() && player.getUseItem() == stack) {
            var data = LunchBoxItem.getLunchBoxData(stack);
            mc.getItemRenderer().renderStatic(
                    data.getSelected(), transformType, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn,
                    mc.level, 0);
        } else {
            mc.getItemRenderer().renderStatic(
                    Items.BUNDLE.getDefaultInstance(), transformType, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn,
                    mc.level, 0);
        }
        matrixStackIn.popPose();
    }

}

