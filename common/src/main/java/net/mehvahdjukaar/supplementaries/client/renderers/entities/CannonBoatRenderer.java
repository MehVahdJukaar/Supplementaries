package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonTrajectoryRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexModels;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CannonBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.IdentityHashMap;
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
        return textures.get(woodType);
    }

    @Override
    public void render(Boat entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        CannonBoatEntity boat = (CannonBoatEntity) entity;
        CannonBlockTile cannon = boat.getInternalCannon();
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


        poseStack.pushPose();
        Vec3 offset = boat.getCannonOffset();
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(RotHlpr.Y180);

        CannonBlockTileRenderer.renderCannonModel(
                (CannonBlockTileRenderer) renderer,
                cannon, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY
        );
        CannonTrajectoryRenderer.render(cannon, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, partialTicks);

        poseStack.popPose();

        poseStack.popPose();

        if (this.entityRenderDispatcher.shouldRenderHitBoxes() || PlatHelper.isDev()) {
            poseStack.pushPose();

            Vec3 worldPos = boat.getInternalCannon().getGlobalPosition(1);
            Vec3 boatPos = boat.position();
            //translate is in world grid
            poseStack.translate(-boatPos.x, -boatPos.y, -boatPos.z);
            poseStack.translate(worldPos.x, worldPos.y, worldPos.z);
            poseStack.translate(0, 0.5, 0);
            VertexModels.renderDebugLine(poseStack, buffer, 0xff00aa33, 0, 1, 0);

            Quaternionf rot = cannon.getReferenceFrame().getRotation(1);
            Vector3f v = rot.transform(new Vector3f(0,0, 1));

            VertexModels.renderDebugLine(poseStack, buffer, 0xffaa0033, v);

            poseStack.popPose();
        }

    }

    private static final Map<WoodType, EntityType<?>> woodToEntities = new IdentityHashMap<>();

}
