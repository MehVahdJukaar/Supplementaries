package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DifferentProspectiveItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private final ModelResourceLocation model3d;
    private final ModelResourceLocation model2d;

    public DifferentProspectiveItemRenderer(ModelResourceLocation model2d, ModelResourceLocation model3d){
        this.model2d = model2d;
        this.model3d = model3d;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext transform, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if (!stack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            matrixStack.pushPose();
            boolean gui = transform == ItemDisplayContext.GUI || transform == ItemDisplayContext.GROUND || transform == ItemDisplayContext.FIXED;

            BakedModel model;
            if (gui) {
                model = ClientHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), model2d);
            } else {
                model = ClientHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), model3d);
            }
            RenderType rendertype = ItemBlockRenderTypes.getRenderType(stack, true);
            VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, stack.hasFoil());
            itemRenderer.renderModelLists(model, stack, light, overlay, matrixStack, vertexconsumer);
            matrixStack.popPose();
        }
    }

}
