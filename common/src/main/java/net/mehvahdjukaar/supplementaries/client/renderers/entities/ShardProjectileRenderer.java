package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

public class ShardProjectileRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean fullBright;

    public ShardProjectileRenderer(EntityRendererProvider.Context context, float scale, boolean fullBright) {
        super(context);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.scale = scale;
        this.fullBright = fullBright;
    }

    public ShardProjectileRenderer(EntityRendererProvider.Context context) {
        this(context, 1.5F, false);
    }

    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, pos);
    }

    public void render(T entity, float p_225623_2_, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
            matrixStack.pushPose();
            //matrixStack.translate(0,-0.125,0);
            matrixStack.scale(this.scale, this.scale, this.scale);
            matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            //matrixStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
            //matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)));

            this.itemRenderer.renderStatic(entity.getItem(), ItemTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer, 0);
            matrixStack.popPose();
            super.render(entity, p_225623_2_, partialTicks, matrixStack, buffer, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

