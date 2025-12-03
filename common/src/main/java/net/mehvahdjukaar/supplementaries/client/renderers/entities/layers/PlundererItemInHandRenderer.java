package net.mehvahdjukaar.supplementaries.client.renderers.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlundererItemInHandRenderer<T extends PlundererEntity, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;
    private static final float X_ROT_MIN = -0.5235988F;
    private static final float X_ROT_MAX = 1.5707964F;

    public PlundererItemInHandRenderer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        PlundererEntity plunderer = (PlundererEntity) livingEntity;
        if (plunderer.isAggressive()) {
            super.renderArmWithItem(livingEntity, itemStack, displayContext, arm, poseStack, buffer, packedLight);
        } else if (plunderer.isUsingSpyglass() && arm == plunderer.getMainArm()) {
            itemStack = Items.SPYGLASS.getDefaultInstance();
            this.renderArmWithSpyglass(livingEntity, itemStack, arm, poseStack, buffer, packedLight);
        }
    }

    private void renderArmWithSpyglass(LivingEntity entity, ItemStack stack, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
        poseStack.pushPose();
        ModelPart modelPart = this.getParentModel().getHead();
        float f = modelPart.xRot;
        modelPart.xRot = Mth.clamp(modelPart.xRot, X_ROT_MIN, X_ROT_MAX);
        modelPart.translateAndRotate(poseStack);
        modelPart.xRot = f;
        CustomHeadLayer.translateToHead(poseStack, false);
        boolean bl = arm == HumanoidArm.LEFT;
        poseStack.translate((bl ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        this.itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.HEAD, false, poseStack, buffer, combinedLight);
        poseStack.popPose();
    }
}
