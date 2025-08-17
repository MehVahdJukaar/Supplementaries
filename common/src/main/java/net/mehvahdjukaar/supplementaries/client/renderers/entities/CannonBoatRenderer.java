package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonTrajectoryRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CannonBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class CannonBoatRenderer extends BoatRenderer {

    private final Map<WoodType, ResourceLocation> textures;

    public CannonBoatRenderer(EntityRendererProvider.Context context) {
        super(context, false);
        this.textures = WoodTypeRegistry.INSTANCE.getValues().stream().collect(ImmutableMap.toImmutableMap((e) -> e,
                (t) -> Supplementaries.res("textures/entity/cannon_boat/" + t.getTexturePath() + ".png")));
    }

    @Override
    public ResourceLocation getTextureLocation(Boat entity) {
        WoodType woodType = ((CannonBoatEntity) entity).getWoodType();
        if (woodType.isVanilla()) {
            return super.getTextureLocation(entity);
        }
        return textures.get(woodType);
    }

    @Override
    public void render(Boat entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        CannonBoatEntity boat = (CannonBoatEntity) entity;
        var cannon = boat.getInternalCannon();
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher()
                .getRenderer(cannon);
        if (renderer == null) return;

        poseStack.pushPose();

        //same as boat
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        float f = (float) entity.getHurtTime() - partialTicks;
        float g = entity.getDamage() - partialTicks;
        if (g < 0.0F) {
            g = 0.0F;
        }

        if (f > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * g / 10.0F * (float) entity.getHurtDir()));
        }

        poseStack.translate(0.0F, -0.375F, 0.0F);

        poseStack.pushPose();
        poseStack.translate(0, 10 / 16f, 0);
        poseStack.scale(4, 4, 4);
        poseStack.mulPose(RotHlpr.Y180);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(
                boat.getBannerItem(), ItemDisplayContext.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer,
                entity.level(), 0
        );

        poseStack.popPose();

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
        if (this.entityRenderDispatcher.shouldRenderHitBoxes()) {

            var vc = buffer.getBuffer(RenderType.lines());
            poseStack.pushPose();
            var p = boat.getCannonGlobalPosition(partialTicks);
            p = p.subtract(boat.position());
            poseStack.translate(p.x, p.y, p.z);
            var pose = poseStack.last();
            vc.addVertex(pose, 0.0F, 0 + 0.25f, 0.0F)
                    .setColor(255, 0, 255, 255)
                    .setNormal(pose, 0, 1, 0);
            vc.addVertex(pose, 0, (float) (0 + 0.25f + 1), 0)
                    .setColor(255, 0, 255, 255)
                    .setNormal(pose, 0, 1, 0);

            poseStack.popPose();
        }

    }

    private static final Map<WoodType, EntityType<?>> woodToEntities = new IdentityHashMap<>();

}
