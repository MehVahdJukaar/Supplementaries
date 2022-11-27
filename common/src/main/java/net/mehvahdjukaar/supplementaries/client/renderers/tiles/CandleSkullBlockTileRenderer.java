package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CandleSkullBlockTileRenderer extends SkullWithWaxTileRenderer<CandleSkullBlockTile> {

    public CandleSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CandleSkullBlockTile tile, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, int pCombinedOverlay) {

        super.render(tile, pPartialTicks, poseStack, buffer, pCombinedLight, pCombinedOverlay);

        BlockState blockstate = tile.getBlockState();

        BlockState candle = tile.getCandle();
        if (!candle.isAir()) {
            candle = candle.setValue(CandleBlock.LIT, blockstate.getValue(CandleBlock.LIT))
                    .setValue(CandleBlock.CANDLES, blockstate.getValue(CandleBlock.CANDLES));

            float yaw = -22.5F * (float) (blockstate.getValue(SkullBlock.ROTATION));

            this.renderWax(poseStack, buffer, pCombinedLight, tile.getWaxTexture(), yaw);

            poseStack.translate(0, 0.5, 0);
            blockRenderer.renderSingleBlock(candle, poseStack, buffer, pCombinedLight, pCombinedOverlay);
        }
    }


}