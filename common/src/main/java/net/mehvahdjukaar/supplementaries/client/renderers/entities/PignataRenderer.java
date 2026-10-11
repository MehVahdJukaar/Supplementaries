package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.common.entities.PignataEntity;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PignataRenderer extends EntityRenderer<PignataEntity> {

    private final ModelPart model;

    public PignataRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = context.bakeLayer(ClientRegistry.PIGNATA_MODEL);
        this.shadowRadius = 0.4f;
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(1, 1).addBox(-6.0F, -16.0F, -7.0F, 12.0F, 12.0F, 14.0F)
                        .texOffs(16, 35).addBox(-3.0F, -10.0F, -9.0F, 6.0F, 4.0F, 2.0F)
                        .texOffs(16, 27).addBox(1.0F, -4.0F, -6.0F, 4.0F, 4.0F, 4.0F)
                        .texOffs(0, 35).addBox(1.0F, -4.0F, 2.0F, 4.0F, 4.0F, 4.0F)
                        .mirror()
                        .texOffs(0, 27).addBox(-5.0F, -4.0F, -6.0F, 4.0F, 4.0F, 4.0F)
                        .texOffs(32, 27).addBox(-5.0F, -4.0F, 2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, Mth.PI, 0.0F));

        body.addOrReplaceChild("left_ear", CubeListBuilder.create()
                        .texOffs(32, 35).addBox(0.0F, 0.0F, -2.0F, 0.0F, 5.0F, 4.0F),
                PartPose.offsetAndRotation(6.0F, -15.0F, -4.0F, 0.0F, 0.0F, -0.2618F));
        body.addOrReplaceChild("right_ear", CubeListBuilder.create()
                        .texOffs(40, 35).addBox(0.0F, 0.0F, -2.0F, 0.0F, 5.0F, 4.0F),
                PartPose.offsetAndRotation(-6.0F, -15.0F, -4.0F, 0.0F, 0.0F, 0.2618F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(PignataEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, light);

        poseStack.pushPose();
        Vec3 followOffset = entity.getMovingAnchorRenderOffset(partialTicks);
        if (followOffset != null) poseStack.translate(followOffset.x, followOffset.y, followOffset.z);

        boolean fromFeet = entity.isSwingingFromFeet();
        float pivotY = fromFeet ? 0 : entity.getBbHeight();
        float sign = fromFeet ? -1 : 1;
        poseStack.translate(0, pivotY, 0);
        poseStack.mulPose(Axis.ZP.rotation(sign * entity.getSwingX(partialTicks)));
        poseStack.mulPose(Axis.XP.rotation(-sign * entity.getSwingZ(partialTicks)));
        poseStack.translate(0, -pivotY, 0);

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        renderModel(model, poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    public static void renderModel(ModelPart model, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);
        poseStack.scale(-1, -1, 1);
        model.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(ModTextures.PIGNATA_TEXTURE)), light, overlay);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(PignataEntity entity) {
        return ModTextures.PIGNATA_TEXTURE;
    }
}
