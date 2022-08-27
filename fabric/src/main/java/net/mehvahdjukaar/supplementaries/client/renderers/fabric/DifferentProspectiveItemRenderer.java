package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DifferentProspectiveItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private final ResourceLocation model3d;
    private final ResourceLocation model2d;

    public DifferentProspectiveItemRenderer(ResourceLocation model2d, ResourceLocation model3d){
        this.model2d = model2d;
        this.model3d = model3d;
    }

    @Override
    public void render(ItemStack stack, ItemTransforms.TransformType transform, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if (!stack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            matrixStack.pushPose();
            boolean gui = transform == ItemTransforms.TransformType.GUI || transform == ItemTransforms.TransformType.GROUND || transform == ItemTransforms.TransformType.FIXED;

            BakedModel model;
            if (gui) {
                model = ClientPlatformHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), model2d);
            } else {
                model = ClientPlatformHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), model3d);
            }
            RenderType rendertype = ItemBlockRenderTypes.getRenderType(stack, true);
            VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, stack.hasFoil());
            itemRenderer.renderModelLists(model, stack, light, overlay, matrixStack, vertexconsumer);
            matrixStack.popPose();
        }
    }

}
