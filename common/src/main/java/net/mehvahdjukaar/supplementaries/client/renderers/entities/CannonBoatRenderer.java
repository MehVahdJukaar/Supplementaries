package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonTrajectoryRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CannonBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Map;

public class CannonBoatRenderer extends BoatRenderer {

    private final Map<WoodType, ResourceLocation> textures;

    public CannonBoatRenderer(EntityRendererProvider.Context context) {
        super(context, false);
        this.textures = WoodTypeRegistry.getTypes().stream().collect(ImmutableMap.toImmutableMap((e) -> e,
                (t) -> Supplementaries.res("textures/entity/cannon_boat/" + t.getTexturePath() + ".png")));
    }

    @Override
    public ResourceLocation getTextureLocation(Boat entity) {
        return textures.get(((CannonBoatEntity) entity).getWoodType());
    }

    @Override
    public void render(Boat entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        CannonBoatEntity boat = (CannonBoatEntity) entity;
        var cannon = boat.getCannon();
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher()
                .getRenderer(cannon);
        if (renderer == null) return;

        poseStack.pushPose();

        Quaternionf boatRot = Axis.YP.rotationDegrees(boat.getCannonGlobalYawOffset());
        poseStack.mulPose(boatRot);
        Vec3 offset = boat.getCannonGlobalOffset();
        poseStack.translate(offset.x, offset.y, offset.z);
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
