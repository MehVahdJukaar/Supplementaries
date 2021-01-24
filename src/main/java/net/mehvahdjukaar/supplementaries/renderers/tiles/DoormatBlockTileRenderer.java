package net.mehvahdjukaar.supplementaries.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.tiles.DoormatBlockTile;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.IReorderingProcessor;

import java.util.List;

public class DoormatBlockTileRenderer extends TileEntityRenderer<DoormatBlockTile> {
    private static final int MAXLINES = 3;

    public DoormatBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(DoormatBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.push();
        //rotate towards direction
        //if(tile.getBlockState().get(HangingSignBlock.HANGING))matrixStackIn.translate(0,0.125, 0);
        matrixStackIn.translate(0.5, 0, 0.5);
        matrixStackIn.rotate(tile.getDirection().getOpposite().getRotation());
        //matrixStackIn.rotate(Const.XN90);

        //animation

        //matrixStackIn.translate(-0.5, -0.875, -0.5);
        //render block
        //matrixStackIn.translate(0.5, 0.5 - 0.1875, 0.5);
        //matrixStackIn.rotate(Const.YN90);


        // render text
        FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
        int i = tile.textHolder.textColor.getTextColor();
        int j = (int) ((double) NativeImage.getRed(i) * 0.4D);
        int k = (int) ((double) NativeImage.getGreen(i) * 0.4D);
        int l = (int) ((double) NativeImage.getBlue(i) * 0.4D);
        int i1 = NativeImage.getCombined(0, l, k, j);



        matrixStackIn.translate(0, 0, -0.0625 - 0.005);
        matrixStackIn.scale(0.010416667F, 0.010416667F, -0.010416667F);


        for(int k1 = 0; k1 < MAXLINES; ++k1) {
            IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(k1, (p_243502_1_) -> {
                List<IReorderingProcessor> list = fontrenderer.trimStringToWidth(p_243502_1_, 75);
                return list.isEmpty() ? IReorderingProcessor.field_242232_a : list.get(0);
            });
            if (ireorderingprocessor != null) {
                float f3 = (float)(-fontrenderer.func_243245_a(ireorderingprocessor) / 2);
                fontrenderer.func_238416_a_(ireorderingprocessor, f3, (float)(k1 * 15 - 20), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
            }
        }


        matrixStackIn.pop();
    }
}