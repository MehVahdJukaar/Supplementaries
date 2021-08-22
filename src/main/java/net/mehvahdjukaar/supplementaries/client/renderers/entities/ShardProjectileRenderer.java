package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ShardProjectileRenderer<T extends Entity & IRendersAsItem> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean fullBright;

    public ShardProjectileRenderer(EntityRendererManager manager, float scale, boolean fullBright) {
        super(manager);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.scale = scale;
        this.fullBright = fullBright;
    }

    public ShardProjectileRenderer(EntityRendererManager manager) {
        this(manager, 1.5F, false);
    }

    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, pos);
    }

    public void render(T entity, float p_225623_2_, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
            matrixStack.pushPose();
            //matrixStack.translate(0,-0.125,0);
            matrixStack.scale(this.scale, this.scale, this.scale);
            matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            //matrixStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
            //matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)));

            this.itemRenderer.renderStatic(entity.getItem(), ItemCameraTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.popPose();
            super.render(entity, p_225623_2_, partialTicks, matrixStack, buffer, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}

