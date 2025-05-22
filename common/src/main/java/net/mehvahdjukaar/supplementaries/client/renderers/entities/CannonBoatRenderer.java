package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonTrajectoryRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CannonBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.entities.CannnonBoatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

public class CannonBoatRenderer extends BoatRenderer {

    public CannonBoatRenderer(EntityRendererProvider.Context context) {
        super(context, false);
    }

    @Override
    public void render(Boat entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        CannnonBoatEntity boat = (CannnonBoatEntity) entity;
        var cannon = boat.getCannon();
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher()
                .getRenderer(cannon);
        if (renderer == null) return;

        poseStack.pushPose();

        Quaternionf boatRot = Axis.YP.rotationDegrees(180.0F - entityYaw);
        poseStack.mulPose(boatRot);
        poseStack.translate(0, 1, 9 / 16f);
        CannonBlockTileRenderer.renderCannonModel(
                (CannonBlockTileRenderer) renderer,
                cannon, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY
        );
        CannonTrajectoryRenderer.render(cannon, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, partialTicks);
        // poseStack.mulPose(RotHlpr.X180);
        // poseStack.mulPose(RotHlpr.Z90);

        poseStack.popPose();
    }
}
