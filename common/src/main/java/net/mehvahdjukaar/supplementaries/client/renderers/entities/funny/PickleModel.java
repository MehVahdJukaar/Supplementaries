package net.mehvahdjukaar.supplementaries.client.renderers.entities.funny;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class PickleModel<T extends LivingEntity> extends PlayerModel<T> {
    public PickleModel(ModelPart modelPart) {
        super(modelPart, false);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0, 13, 0));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0, 13, 0));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 2)
                        .addBox(-4.0F, -2.0F, -4.0F, 8.0F, 14.0F, 8.0F, false),
                PartPose.ZERO);

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(2, 18)
                        .addBox(-1.0F, -0.5F, -1.0F, 2.0F, 8.0F, 2.0F, false)
                , PartPose.offset(5.0F, 2.5F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(16, 18)
                        .addBox(-1.0F, -0.5F, -1.0F, 2.0F, 8.0F, 2.0F, false)
                , PartPose.offset(-5.0F, 2.5F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(3.85F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, true)
                , PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(16, 24).addBox(-5.85F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, false)
                , PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void translateToHand(HumanoidArm handSide, PoseStack matrixStack) {
        matrixStack.translate(0, 0.5, 0);
        ModelPart arm = this.getArm(handSide);

        float f = 1F * (float) (handSide == HumanoidArm.RIGHT ? 1 : -1);
        arm.x += f;
        arm.y -= 1;
        arm.z += 1;
        arm.translateAndRotate(matrixStack);
        arm.z -= 1;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {

        matrixStack.pushPose();
        matrixStack.translate(0, this.riding ? -0.5 : 0.5f, 0);

        super.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, color);
        matrixStack.popPose();
    }

    @Override
    public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (swimAmount > 0 && player.isVisuallySwimming()) {
            this.body.yRot = this.rotlerpRad(limbSwing, this.body.yRot, (-(float) Math.PI / 30F));
        } else {
            float f1 = player.getFallFlyingTicks();
            if (f1 > 0.01) {
                f1 += Minecraft.getInstance().getFrameTime();
                float inclination = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
                leftArm.xRot = inclination * (float) Math.PI;
                rightArm.xRot = inclination * (float) Math.PI;
            }
        }
        //float f = (MathHelper.rotLerp(limbSwingAmount, player.yBodyRotO, player.yBodyRot))%360;
        //this.body.yRot = -f / (180F / (float) Math.PI);
    }


    public static class PickleArmor<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A> {

        public PickleArmor(RenderLayerParent<T, M> renderer, A modelChest, ModelManager manager) {
            super(renderer, modelChest, modelChest, manager);
        }

        @Override
        public void setPartVisibility(A modelIn, EquipmentSlot slotIn) {
            modelIn.setAllVisible(false);
            boolean head = slotIn == EquipmentSlot.HEAD;
            modelIn.hat.visible = head;
            modelIn.head.visible = head;
            modelIn.head.copyFrom(modelIn.body);
            modelIn.head.y = 13;
            modelIn.hat.copyFrom(modelIn.head);
        }

        @Override
        public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isCrouching()) return;
            super.render(matrixStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }

    public static class PickleElytra<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

        public PickleElytra(RenderLayerParent<T, M> renderer, EntityModelSet modelSet) {
            super(renderer, modelSet);
        }

        @Override
        public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            matrixStack.translate(0, 0.625, 0.09375);
            matrixStack.scale(0.625f, 0.625f, 0.625f);

            super.render(matrixStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }

        @ForgeOverride
        public boolean shouldRender(ItemStack stack, T entity) {
            return !entity.isCrouching() && stack.getItem() == Items.ELYTRA;
        }
    }
}
