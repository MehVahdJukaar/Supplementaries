package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoubleSkullBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DoubleSkullBlockTileRenderer extends AbstractSkullBlockTileRenderer<DoubleSkullBlockTile> {


    public DoubleSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DoubleSkullBlockTile tile, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, int pCombinedOverlay) {
        super.render(tile, pPartialTicks, poseStack, buffer, pCombinedLight, pCombinedOverlay);

        BlockEntity upSkull = tile.getSkullTileUp();
        if(upSkull != null){

            float yawUp = -22.5F * (float) upSkull.getBlockState().getValue(SkullBlock.ROTATION);

            poseStack.translate(0, 0.5, 0);


            renderInner(upSkull, pPartialTicks, poseStack, buffer, pCombinedLight, pCombinedOverlay);

           // blockRenderer.renderSingleBlock(upSkull, poseStack, buffer, pCombinedLight, pCombinedOverlay, EmptyModelData.INSTANCE);

            ResourceLocation texture = tile.getWaxTexture();
            if (texture != null) {
                this.renderWax(poseStack, buffer, pCombinedLight, texture,yawUp);
            }
        }

    }

}