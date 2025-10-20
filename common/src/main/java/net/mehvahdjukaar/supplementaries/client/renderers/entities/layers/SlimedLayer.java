package net.mehvahdjukaar.supplementaries.client.renderers.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.IModelPartExtension;
import net.mehvahdjukaar.supplementaries.client.renderers.SlimedRenderTypes;
import net.mehvahdjukaar.supplementaries.common.entities.data.SlimedData;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.EMFCompat;
import net.mehvahdjukaar.supplementaries.mixins.AgeableListAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;


public class SlimedLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public SlimedLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        float alpha = SlimedData.getAlpha(entity, partialTicks);

        if (alpha == 0) return;

        int width = 64;
        int height = 64;
        M model = this.getParentModel();
        ModelPart modelPart = null;
        if (CompatHandler.EMF) {
            modelPart = EMFCompat.getFirstEMFModelPart(model);
        }
        if (modelPart == null && model instanceof AgeableListAccessor al) {
            for (ModelPart v : al.invokeBodyParts()) {
                modelPart = v;
                break;
            }
        } else if (model instanceof HierarchicalModel<?> m) {
            modelPart = m.root();
        }

        if (modelPart != null) {
            IModelPartExtension part = (IModelPartExtension) (Object) modelPart;
            height = part.supp$getTextHeight();
            width = part.supp$getTextWidth();
        }

        VertexConsumer consumer = buffer.getBuffer(SlimedRenderTypes.get(width, height));

        poseStack.pushPose();
        model.renderToBuffer(poseStack, consumer, packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                FastColor.ARGB32.color((int) (alpha * 255f), 255, 255, 255));
        poseStack.popPose();
    }
}

