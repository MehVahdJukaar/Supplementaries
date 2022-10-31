package net.mehvahdjukaar.supplementaries.client.renderers.entities.funny;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class JarredModel<T extends LivingEntity> extends PlayerModel<T> {
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;

    public JarredModel(ModelPart modelPart) {
        super(modelPart, false);

        ModelPart head = modelPart.getChild("head");
        this.eyeLeft = head.getChild("left_eye");
        this.eyeRight = head.getChild("right_eye");
    }

    public static LayerDefinition createMesh() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -2.0F, -5.0F, 10.0F, 14.0F, 10.0F)
                        .texOffs(40, 0)
                        .addBox(-3.0F, -4.0F, -3.0F, 6.0F, 3.0F, 6.0F)
                        .texOffs(0, 24)
                        .addBox(-4.0F, 0.0F, -4.0F, 8.0F, 11.0F, 8.0F)
                , PartPose.ZERO);


        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(51, 1)
                        .addBox(3.9F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, true)
                , PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(46, 1)
                        .addBox(-4.9F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, false)
                , PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(42, 10)
                        .addBox(-3.0F, -2.999F, -1.5F, 6.0F, 3.0F, 3.0F, false)
                        .texOffs(45, 12).addBox(-2.0F, -2.999F, 1.5F, 4.0F, 3.0F, 1.0F, false)
                        .texOffs(40, 16).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 1.0F, 3.0F, false)
                        .texOffs(40, 20).addBox(-1.0F, 1.0F, 0.5F, 2.0F, 1.0F, 2.0F, false)
                , PartPose.offset(0.0F, 5.0F, 0.0F));

        head.addOrReplaceChild("left_eye", CubeListBuilder.create()
                        .texOffs(30, 6)
                        .addBox(-3.0F, -1.0F, -2.499F, 2.0F, 2.0F, 2.0F, false)
                , PartPose.ZERO);
        head.addOrReplaceChild("right_eye", CubeListBuilder.create()
                        .texOffs(30, 6)
                        .addBox(1.0F, 0.0F, -2.499F, 2.0F, 2.0F, 2.0F, false)
                , PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void translateToHand(HumanoidArm handSide, PoseStack matrixStack) {
        matrixStack.translate(0, 0.8, -0.5);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        ModelPart arm = this.getArm(handSide);

        float f = 1F * (handSide == HumanoidArm.RIGHT ? 1 : -1);
        arm.x += f;
        arm.y -= 1;
        arm.z += 1;
        //arm.translateAndRotate(matrixStack);
        arm.z -= 1;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        matrixStack.pushPose();
        matrixStack.translate(0, this.riding ? -0.5 : 0.5f, 0);

        super.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        matrixStack.popPose();
    }


    @Override
    public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (swimAmount > 0 && player.isVisuallySwimming()) {
            this.body.yRot = this.rotlerpRad(limbSwing, this.body.yRot, (-(float) Math.PI / 30F));
        }
        //this.head.copyFrom(this.body);

        head.y = 6f + Mth.sin(limbSwing / 3f) / 2f;

        eyeRight.x = Mth.cos(ageInTicks / 16f) / 4f;
        eyeRight.y = Mth.sin(ageInTicks / 7f) / 4f;

        eyeLeft.x = Mth.cos(ageInTicks / 12f) / 4f;
        eyeLeft.y = Mth.cos(ageInTicks / 7f) / 4f;

        //float f = (MathHelper.rotLerp(limbSwingAmount, player.yBodyRotO, player.yBodyRot))%360;
        //this.body.yRot = -f / (180F / (float) Math.PI);
    }
}
