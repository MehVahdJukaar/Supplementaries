package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
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
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class SlingshotProjectileRenderer<T extends Entity & IRendersAsItem> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale = 1.25f;

    public SlingshotProjectileRenderer(EntityRendererManager manager) {
        super(manager);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    private Integer light = null;

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        if(this.light == null){
            Item item = entity.getItem().getItem();
            if(item instanceof BlockItem) {
                Block b = ((BlockItem) item).getBlock();
                this.light = b.getBlock().getLightValue(b.defaultBlockState(), entity.level, pos);
            }
            else this.light = 0;
        }
        return this.light;
    }

    @Override
    public void render(T entity, float p_225623_2_, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        if (entity.tickCount >= 3 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
            matrixStack.pushPose();
            matrixStack.translate(0,0.25,0);

            matrixStack.mulPose(Vector3f.YN.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot)));
            matrixStack.mulPose(Vector3f.ZN.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)));


            matrixStack.scale(this.scale, this.scale, this.scale);


           // matrixStack.mulPose(Vector3f.XN.rotation(entity.tickCount/20f % 360 + partialTicks));
            //matrixStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
            //matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)));

            this.itemRenderer.renderStatic(entity.getItem(), ItemCameraTransforms.TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.popPose();
            super.render(entity, p_225623_2_, partialTicks, matrixStack, buffer, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}

