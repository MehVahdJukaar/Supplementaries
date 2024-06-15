package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;

import static net.mehvahdjukaar.supplementaries.client.renderers.entities.ImprovedThrownItemRenderer.MIN_CAMERA_DISTANCE_SQUARED;

public class SlingshotProjectileRenderer<T extends SlingshotProjectileEntity & ItemSupplier> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;


    public SlingshotProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return entity.getLightEmission();
    }

    @Override
    public void render(T entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (entity.tickCount < 2 && this.entityRenderDispatcher.camera.getPosition().distanceToSqr(entity.position()) < MIN_CAMERA_DISTANCE_SQUARED) {
            return;
        }
        //centers everything to hitbox y = 0 (rendered hitbox will be lowered)
        poseStack.translate(0, -entity.getBbHeight() / 2f, 0);

        poseStack.pushPose();
        poseStack.translate(0, 0.25, 0);

        poseStack.mulPose(Axis.YN.rotationDegrees(180 - Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.ZN.rotationDegrees(Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));

        float scale = (float) (double) ClientConfigs.Items.SLINGSHOT_PROJECTILE_SCALE.get();
        poseStack.scale(scale, scale, scale);

        this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), 0);
        poseStack.popPose();
        super.render(entity, pEntityYaw, partialTicks, poseStack, buffer, light);

    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

