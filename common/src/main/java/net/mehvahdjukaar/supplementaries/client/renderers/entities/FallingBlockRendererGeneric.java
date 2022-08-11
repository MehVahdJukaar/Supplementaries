package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlockRendererGeneric<T extends FallingBlockEntity> extends EntityRenderer<T> {

    public FallingBlockRendererGeneric(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(T pEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(T entity, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pPackedLight) {
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() == RenderShape.MODEL) {
            Level level = entity.getLevel();
            BlockPos pos = entity.blockPosition();
            boolean isJustSpawned = Math.abs(entity.getY() - pos.getY()) < 0.02 && entity.tickCount < 0 && state != level.getBlockState(pos);
            if (!isJustSpawned && state.getRenderShape() != RenderShape.INVISIBLE) {
                poseStack.pushPose();
                BlockPos blockpos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
                poseStack.translate(-0.5D, 0.0D, -0.5D);
                BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

                RenderUtil.renderBlock(state.getSeed(entity.getStartPos()),
                        poseStack, buffer, state, level, blockpos, dispatcher);
                poseStack.popPose();
                super.render(entity, pEntityYaw, pPartialTicks, poseStack, buffer, pPackedLight);
            }
        }
    }


}
