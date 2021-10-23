package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.block.tiles.DoormatBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DoormatBlockTileRenderer implements BlockEntityRenderer<DoormatBlockTile> {

    private final Font FONT;

    public DoormatBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        FONT = Minecraft.getInstance().font;
    }

    @Override
    public void render(DoormatBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();

        matrixStackIn.translate(0.5, 0, 0.5);
        matrixStackIn.mulPose(Const.rot(tile.getDirection().getOpposite()));

        // render text
        int i = tile.textHolder.textColor.getTextColor();
        int j = (int) ((double) NativeImage.getR(i) * 0.4D);
        int k = (int) ((double) NativeImage.getG(i) * 0.4D);
        int l = (int) ((double) NativeImage.getB(i) * 0.4D);
        int i1 = NativeImage.combine(0, l, k, j);

        matrixStackIn.translate(0, 0, -0.0625 - 0.005);
        matrixStackIn.scale(0.010416667F, 0.010416667F, -0.010416667F);

        for (int k1 = 0; k1 < tile.textHolder.size; ++k1) {
            FormattedCharSequence formattedCharSequence = tile.textHolder.getRenderText(k1, (p_243502_1_) -> {
                List<FormattedCharSequence> list = FONT.split(p_243502_1_, 75);
                return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
            });
            if (formattedCharSequence != null) {
                float f3 = (float) (-FONT.width(formattedCharSequence) / 2);
                FONT.drawInBatch(formattedCharSequence, f3, (float) (k1 * 15 - 20), i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);
            }
        }

        matrixStackIn.popPose();
    }
}