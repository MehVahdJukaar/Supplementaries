package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class QuiverLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final ItemRenderer itemRenderer;

    public QuiverLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        QuiverMode mode = ClientConfigs.Items.QUIVER_RENDER_MODE.get();
        if (mode == QuiverMode.HIDDEN) return;

        ItemStack quiver = QuiverItem.getQuiver(livingEntity);
        if (livingEntity.getMainHandItem() == quiver || livingEntity.getOffhandItem() == quiver) {
            return;
        }

        if (!quiver.isEmpty()) {
            this.getParentModel().body.translateAndRotate(poseStack);

            boolean flipped = livingEntity.getMainArm() == HumanoidArm.RIGHT;


            if (mode == QuiverMode.THIGH) {
                boolean hasArmor = livingEntity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorItem;
                double offset = hasArmor ? ClientConfigs.Items.QUIVER_ARMOR_OFFSET.get() : 0;

                if (flipped) {
                    var old = this.getParentModel().leftLeg.xRot;
                    this.getParentModel().leftLeg.xRot = old * 0.3f;
                    this.getParentModel().leftLeg.translateAndRotate(poseStack);
                    this.getParentModel().leftLeg.xRot = old;
                    poseStack.translate(0, -1 / 16f, -2.5 / 16f);
                    poseStack.translate(offset == -1 ? 3.5 / 16f : 3 / 16f + offset, 0, 0);

                } else {
                    var old = this.getParentModel().rightLeg.xRot;
                    this.getParentModel().rightLeg.xRot = old * 0.3f;
                    this.getParentModel().rightLeg.translateAndRotate(poseStack);
                    this.getParentModel().rightLeg.xRot = old;
                    poseStack.translate(0, -1 / 16f, -2.5 / 16f);
                    poseStack.translate(offset == -1 ? -3.5 / 16f : -3 / 16f + offset, 0, 0);
                }

            } else {
                boolean hasArmor = livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem;
                double offset = hasArmor ? ClientConfigs.Items.QUIVER_ARMOR_OFFSET.get() : 0;

                if (mode == QuiverMode.HIP) {
                    poseStack.translate(0, 0.1, offset == -1 ? 3.5 / 16f : 3 / 16f + offset);
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
                    if (flipped) poseStack.scale(-1, 1, -1);

                    poseStack.translate(0, 0.4, -3 / 16f);
                    poseStack.mulPose(Vector3f.XN.rotationDegrees(-22.5f));
                } else {
                    poseStack.translate(0, 0.1, offset == -1 ? 4 / 16f : 3 / 16f + offset);
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
                    if (flipped) poseStack.scale(-1, 1, -1);

                    poseStack.translate(0, 0, -0.125);
                }
            }
            itemRenderer.renderStatic(livingEntity, quiver, ItemTransforms.TransformType.HEAD, false,
                    poseStack, buffer, livingEntity.level, packedLight, OverlayTexture.NO_OVERLAY, 0);


        }
    }

    public enum QuiverMode {
        HIDDEN,
        BACK,
        HIP,
        THIGH
    }

}
