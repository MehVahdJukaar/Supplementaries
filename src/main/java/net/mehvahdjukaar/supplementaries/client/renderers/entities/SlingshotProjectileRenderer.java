package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.entities.SlingshotProjectileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class SlingshotProjectileRenderer<T extends SlingshotProjectileEntity & IRendersAsItem> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;


    public SlingshotProjectileRenderer(EntityRendererManager manager) {
        super(manager);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public Vector3d getRenderOffset(T p_225627_1_, float p_225627_2_) {
        return super.getRenderOffset(p_225627_1_, p_225627_2_);
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return entity.light.get();
    }

    @Override
    public void render(T entity, float p_225623_2_, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        //centers everything to hitbox y = 0 (rendered hitbox will be lowered)
        matrixStack.translate(0,-entity.getBbHeight()/2f, 0);
        if (entity.tickCount >= 3 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
            matrixStack.pushPose();
            matrixStack.translate(0,0.25,0);

            matrixStack.mulPose(Vector3f.YN.rotationDegrees(180 - MathHelper.rotLerp(partialTicks, entity.yRotO, entity.yRot)));
            matrixStack.mulPose(Vector3f.ZN.rotationDegrees(MathHelper.rotLerp(partialTicks, entity.xRotO, entity.xRot)));

            float scale = ClientConfigs.cached.SLINGSHOT_PROJECTILE_SCALE;
            matrixStack.scale(scale, scale, scale);

            this.itemRenderer.renderStatic(entity.getItem(), ItemCameraTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.popPose();
            super.render(entity, p_225623_2_, partialTicks, matrixStack, buffer, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(SlingshotProjectileEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}

