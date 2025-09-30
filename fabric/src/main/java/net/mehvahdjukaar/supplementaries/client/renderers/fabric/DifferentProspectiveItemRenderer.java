package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.frozenblock.lib.item.api.ItemBlockStateTagUtils;
import net.frozenblock.lib.item.api.removable.RemovableItemTags;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLJsonMapDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class DifferentProspectiveItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private final ModelResourceLocation model3d;
    private final ModelResourceLocation model2d;
    private final Set<ItemDisplayContext> isFirst;

    public DifferentProspectiveItemRenderer(ModelResourceLocation first, ModelResourceLocation second,
                                            Set<ItemDisplayContext> isFirst){
        this.model2d = first;
        this.model3d = second;
        this.isFirst = isFirst;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext transform, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if (!stack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            matrixStack.pushPose();
            boolean isFirst = this.isFirst.contains(transform);

            BakedModel model;
            if (isFirst) {
                model = ClientHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), model2d);
            } else {
                model = ClientHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), model3d);
            }
            Preconditions.checkNotNull(model, "Model not found for item: " + stack + " " + (gui ? model2d : model3d));
            RenderType rendertype = ItemBlockRenderTypes.getRenderType(stack, true);
            VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, stack.hasFoil());
            itemRenderer.renderModelLists(model, stack, light, overlay, matrixStack, vertexconsumer);
            matrixStack.popPose();
        }
    }

}
