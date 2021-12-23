package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoubleSkullBlockTile;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleSkullBlockTileRenderer extends AbstractSkullBlockTileRenderer<DoubleSkullBlockTile> {


    public DoubleSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DoubleSkullBlockTile tile, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, int pCombinedOverlay) {
        float f = 0;//tile.getMouthAnimation(pPartialTicks);
        try {
            BlockState blockstate = tile.getBlockState();

            float yaw = 22.5F * (float) blockstate.getValue(SkullBlock.ROTATION);
            SkullBlock.Type type = tile.getSkullType();
            SkullModelBase modelBase = this.modelByType.get(type);
            RenderType renderType = SkullBlockRenderer.getRenderType(type, tile.getOwnerProfile());
            SkullBlockRenderer.renderSkull(null, yaw, f, poseStack, buffer, pCombinedLight, modelBase, renderType);

            poseStack.translate(0, 0.5, 0);

            float yawUp = 22.5F * tile.getUpRotation();
            SkullBlock.Type typeUp = tile.getSkullTypeUp();
            SkullModelBase modelBaseUp = this.modelByType.get(typeUp);
            RenderType renderTypeUp = SkullBlockRenderer.getRenderType(typeUp, tile.getOwnerProfileUp());
            SkullBlockRenderer.renderSkull(null, yawUp, f, poseStack, buffer, pCombinedLight, modelBaseUp, renderTypeUp);

            ResourceLocation texture = tile.getWaxTexture();
            if (texture != null) {
                this.renderOverlay(poseStack, buffer, pCombinedLight, texture, yawUp);
            }


        } catch (Exception e) {
            int a = 1;
        }
    }


}