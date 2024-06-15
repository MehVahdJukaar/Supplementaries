package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;

import static net.mehvahdjukaar.supplementaries.client.renderers.entities.ImprovedThrownItemRenderer.MIN_CAMERA_DISTANCE_SQUARED;

public class CannonballRenderer<T extends Projectile> extends EntityRenderer<T> {
    private final ModelPart model;
    private final float scale;

    public CannonballRenderer(EntityRendererProvider.Context context, float scale) {
        super(context);
        this.model = context.bakeLayer(ClientRegistry.CANNONBALL_MODEL);
        this.scale = scale;
    }

    @Override
    public void render(T entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (entity.tickCount < 2 && this.entityRenderDispatcher.camera.getPosition().distanceToSqr(entity.position()) < MIN_CAMERA_DISTANCE_SQUARED) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() / 2, 0);

        poseStack.mulPose(Axis.YN.rotationDegrees(180 - Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XN.rotationDegrees(-Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));

        poseStack.scale(scale, scale, scale);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));

        model.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, pEntityYaw, partialTicks, poseStack, buffer, light);

    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return ModTextures.CANNONBALL_TEXTURE;
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        meshDefinition.getRoot().addOrReplaceChild("ball", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.5F, -3.5F, -3.5F, 7, 7, 7),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 32, 16);
    }
}

