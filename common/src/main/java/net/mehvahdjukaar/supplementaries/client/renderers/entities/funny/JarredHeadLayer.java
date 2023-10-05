package net.mehvahdjukaar.supplementaries.client.renderers.entities.funny;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class JarredHeadLayer<T extends Player, M extends HumanoidModel<T> & HeadedModel> extends RenderLayer<T, M> {
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;
    private final ModelPart head;
    private final ModelPart model;

    public JarredHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = entityModelSet.bakeLayer(ClientRegistry.JAR_MODEL);
        this.head = model.getChild("head");
        this.eyeLeft = head.getChild("left_eye");
        this.eyeRight = head.getChild("right_eye");
    }

    public static LayerDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 44)
                        .addBox(-5.0F, -2.0F, -5.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(40, 0)
                        .addBox(-3.0F, -4.001F, -3.0F, 6.0F, 3.0F, 6.0F)
                        .texOffs(0, 24)
                        .addBox(-4.0F, -1.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                , PartPose.ZERO);

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
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight,
                       T player, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!PickleData.isActive(player.getGameProfile().getId())) return;

        matrixStack.pushPose();

        M parent = this.getParentModel();
        ModelPart parentHead = parent.getHead();
        float bodyYRot = Mth.rotLerp(partialTick, player.yRotO, player.getYRot()) * Mth.DEG_TO_RAD;
        float bodyXRot = Mth.rotLerp(partialTick, player.xRotO, player.getXRot()) * Mth.DEG_TO_RAD;
        parentHead.translateAndRotate(matrixStack);

        float deltaTime = Minecraft.getInstance().getDeltaFrameTime();

        float viscosity = 0.17f;
        head.yRot = rotlerpRad(deltaTime * viscosity, head.yRot, bodyYRot + parentHead.yRot);
        head.xRot = rotlerpRad(deltaTime * viscosity, head.xRot, bodyXRot + parentHead.xRot);
        float k = -Mth.PI * 2;
        head.yRot = -bodyYRot - parentHead.yRot + head.yRot + k;
        head.xRot = -bodyXRot - parentHead.xRot + head.xRot + k;
        float g = 14 / 16f;
        matrixStack.scale(-g, -g, g);
        matrixStack.translate(-0.5, 0.5, -0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(ModTextures.JAR_MAN));

        matrixStack.pushPose();
        matrixStack.translate(0.5F, 0.0F, 0.5F);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);

        this.model.yRot = 0;
        if (parent.swimAmount > 0 && player.isVisuallySwimming()) {
            //this.model.yRot = this.rotlerpRad(limbSwing, this.model.yRot, (-(float) Math.PI / 30F));
        }
        //this.head.copyFrom(this.body);

        this.head.y = 3.5f + Mth.sin((ageInTicks / 11 + limbSwing / 5)) / 2.5f;

        eyeRight.x = Mth.cos(ageInTicks / 16f) / 4f;
        eyeRight.y = Mth.sin(ageInTicks / 7f) / 4f;

        eyeLeft.x = Mth.cos(ageInTicks / 12f) / 4f;
        eyeLeft.y = Mth.cos(ageInTicks / 7f) / 4f;

        model.render(matrixStack, vc, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.popPose();

        matrixStack.popPose();

        //compensates for this
        head.yRot += (parentHead.yRot + bodyYRot - k);
        head.xRot += parentHead.xRot + bodyXRot - k;
    }


    protected float rotlerpRad(float angle, float maxAngle, float mul) {
        float f = (mul - maxAngle) % 6.2831855F;
        if (f < -3.1415927F) {
            f += 6.2831855F;
        }

        if (f >= 3.1415927F) {
            f -= 6.2831855F;
        }

        return maxAngle + angle * f;
    }
}

