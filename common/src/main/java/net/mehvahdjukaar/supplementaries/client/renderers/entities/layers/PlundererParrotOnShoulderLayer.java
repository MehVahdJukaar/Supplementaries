package net.mehvahdjukaar.supplementaries.client.renderers.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;

public class PlundererParrotOnShoulderLayer<T extends PlundererEntity, A extends EntityModel<T>> extends RenderLayer<T, A> {
    private final ParrotModel model;

    public PlundererParrotOnShoulderLayer(RenderLayerParent<T, A> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new ParrotModel(modelSet.bakeLayer(ModelLayers.PARROT));
    }

    public static void renderDancing(ParrotModel model, PoseStack poseStack, VertexConsumer buffer,
                                     int packedLight, int packedOverlay, float limbSwing, float limbSwingAmount,
                                     float netHeadYaw, float headPitch, int tickCount, float bob) {
        model.prepare(ParrotModel.State.PARTY);
        model.setupAnim(ParrotModel.State.PARTY, tickCount, limbSwing, limbSwingAmount, bob, netHeadYaw, headPitch);
        model.root().render(poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.render(poseStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, netHeadYaw, headPitch, true);
        this.render(poseStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, netHeadYaw, headPitch, false);
    }

    private void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulder) {
        CompoundTag compoundTag = leftShoulder ? livingEntity.getShoulderEntityLeft() : livingEntity.getShoulderEntityRight();
        EntityType.byString(compoundTag.getString("id")).filter((entityType) -> entityType == EntityType.PARROT).ifPresent((entityType) -> {
            poseStack.pushPose();
            poseStack.translate(leftShoulder ? 0.4F : -0.4F, livingEntity.isCrouching() ? -1.3F : -1.5F, 0.0F);
            Parrot.Variant variant = Parrot.Variant.byId(compoundTag.getInt("Variant"));
            VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(ParrotRenderer.getVariantTexture(variant)));

            if (compoundTag.getBoolean("record_playing")) {
                renderDancing(model, poseStack, vertexConsumer, packedLight,
                        OverlayTexture.NO_OVERLAY, limbSwing, limbSwingAmount, netHeadYaw, headPitch,
                        livingEntity.tickCount, 0);
            } else {
                this.model.renderOnShoulder(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, limbSwing, limbSwingAmount, netHeadYaw, headPitch, livingEntity.tickCount);
            }
            poseStack.popPose();
        });
    }
}
