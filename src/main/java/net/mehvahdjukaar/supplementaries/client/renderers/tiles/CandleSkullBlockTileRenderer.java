package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.block.tiles.CandleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CandleSkullBlockTileRenderer extends AbstractSkullBlockTileRenderer<CandleSkullBlockTile> {

    public CandleSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CandleSkullBlockTile tile, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight, int pCombinedOverlay) {

        BlockState blockstate = tile.getBlockState();
        float yaw = 22.5F * (float) blockstate.getValue(SkullBlock.ROTATION);

        this.renderSkull(tile, poseStack, buffer, pCombinedLight, yaw);

        BlockState candle = tile.getCandle();
        if (!candle.isAir()) {


            this.renderOverlay(poseStack, buffer, pCombinedLight, Textures.SKULL_CANDLES_TEXTURES.get(tile.getCandleColor()), yaw);

            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-yaw));
            poseStack.translate(-0.5, 0.5, -0.5);
            blockRenderer.renderSingleBlock(candle, poseStack, buffer, pCombinedLight, pCombinedOverlay, EmptyModelData.INSTANCE);
        }
    }


}