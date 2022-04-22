package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.phys.Vec3;

public class SlingshotProjectileRenderer<T extends SlingshotProjectileEntity & ItemSupplier> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;


    public SlingshotProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public Vec3 getRenderOffset(T p_225627_1_, float p_225627_2_) {
        return super.getRenderOffset(p_225627_1_, p_225627_2_);
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return entity.light.get();
    }

    @Override
    public void render(T entity, float pEntityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light) {
        //centers everything to hitbox y = 0 (rendered hitbox will be lowered)
        matrixStack.translate(0, -entity.getBbHeight() / 2f, 0);
        if (entity.tickCount >= 3 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
            matrixStack.pushPose();
            matrixStack.translate(0, 0.25, 0);

            matrixStack.mulPose(Vector3f.YN.rotationDegrees(180 - Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot())));
            matrixStack.mulPose(Vector3f.ZN.rotationDegrees(Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));

            float scale = ClientConfigs.cached.SLINGSHOT_PROJECTILE_SCALE;
            matrixStack.scale(scale, scale, scale);

            this.itemRenderer.renderStatic(entity.getItem(), ItemTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer, 0);
            matrixStack.popPose();
            super.render(entity, pEntityYaw, partialTicks, matrixStack, buffer, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(SlingshotProjectileEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

