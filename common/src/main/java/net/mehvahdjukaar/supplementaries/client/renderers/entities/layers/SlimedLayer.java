package net.mehvahdjukaar.supplementaries.client.renderers.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.IModelPartExtension;
import net.mehvahdjukaar.supplementaries.client.renderers.SlimedRenderType;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.mixins.AgeableListAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public class SlimedLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public SlimedLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        float alpha = ISlimeable.getAlpha(entity, partialTicks);

        if (alpha == 0) return;

        int width = 64;
        int height = 64;
        M model = this.getParentModel();
        if (model instanceof AgeableListAccessor al) {
            for (var v : al.invokeBodyParts()) {
                IModelPartExtension part = (IModelPartExtension) (Object) v;
                height = part.supp$getTextHeight();
                width = part.supp$getTextWidth();
                break;
            }
        } else if (model instanceof HierarchicalModel<?> m) {
            IModelPartExtension part = (IModelPartExtension) (Object) m.root();
            height = part.supp$getTextHeight();
            width = part.supp$getTextWidth();
        }

        VertexConsumer consumer = buffer.getBuffer(SlimedRenderType.get(width, height));

        poseStack.pushPose();
        model.renderToBuffer(poseStack, consumer, packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                1.0F, 1, 1.0F, alpha);
        poseStack.popPose();
    }
}

