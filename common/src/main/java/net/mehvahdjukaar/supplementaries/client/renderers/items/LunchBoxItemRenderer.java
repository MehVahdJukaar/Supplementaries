package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class LunchBoxItemRenderer extends ItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pose, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        //render block
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ItemRenderer itemRenderer = mc.getItemRenderer();
        var data = LunchBoxItem.getLunchBoxData(stack);
        if (transformType != ItemDisplayContext.GUI && player != null && player.isUsingItem() && player.getUseItem() == stack) {
            itemRenderer.renderStatic(
                    data.getSelected(), transformType, combinedLightIn, combinedOverlayIn, pose, buffer,
                    mc.level, 0);
        } else {
            BakedModel model = ClientHelper.getModel(mc.getModelManager(),
                    data.canEatFrom() ? ClientRegistry.LUNCH_BOX_OPEN_ITEM_MODEL : ClientRegistry.LUNCH_BOX_ITEM_MODEL);
            itemRenderer.render(stack, transformType, false, pose, buffer,
                    combinedLightIn, combinedOverlayIn, model);
        }
        pose.popPose();
    }

}

