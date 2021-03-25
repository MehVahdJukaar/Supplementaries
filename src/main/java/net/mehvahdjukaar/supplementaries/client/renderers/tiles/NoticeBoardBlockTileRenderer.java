package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.TextUtil;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.RequestMapDataFromServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AbstractMapItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.world.storage.MapData;

import java.util.List;


public class NoticeBoardBlockTileRenderer extends TileEntityRenderer<NoticeBoardBlockTile> {

    public NoticeBoardBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }



    @Override
    public void render(NoticeBoardBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if(tile.textVisible){
            //TODO: fix book with nothing in it
            int frontLight = tile.getFrontLight();
            ItemStack stack = tile.getItem(0);

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.getYaw()));
            matrixStackIn.translate(0, 0, 0.5);

            //render map
            MapData mapdata = FilledMapItem.getOrCreateSavedData(stack, tile.getLevel());
            if(stack.getItem() instanceof AbstractMapItem) {
                if (mapdata != null) {
                    matrixStackIn.pushPose();
                    matrixStackIn.translate(0, 0, 0.008);
                    matrixStackIn.scale(0.0078125F, -0.0078125F, -0.0078125F);
                    matrixStackIn.translate(-64.0D, -64.0D, 0.0D);

                    Minecraft.getInstance().gameRenderer.getMapRenderer().render(matrixStackIn, bufferIn, mapdata, true, frontLight);
                    matrixStackIn.popPose();
                }
                else{
                    //request map data from server
                    PlayerEntity player = Minecraft.getInstance().player;
                    NetworkHandler.INSTANCE.sendToServer(new RequestMapDataFromServerPacket(tile.getBlockPos(),player.getUUID()));
                }
                matrixStackIn.popPose();
                return;
            }

            //render book
            String page = tile.getText();
            if (!(page == null || page.equals(""))) {

                FontRenderer fontrenderer = this.renderer.getFont();

                matrixStackIn.pushPose();
                matrixStackIn.translate(0,0.5,0.008);

                float d0;
                if (tile.getAxis()) {
                    d0 = 0.8f * 0.7f;
                } else {
                    d0 = 0.6f * 0.7f;
                }

                String bookName = tile.getItem(0).getHoverName().getString().toLowerCase();
                if(bookName.equals("credits")){
                    TextUtil.renderCredits(matrixStackIn,bufferIn,frontLight,fontrenderer,d0);
                    matrixStackIn.popPose();
                    matrixStackIn.popPose();
                    return;
                }


                int i = tile.getTextColor().getTextColor();
                int r = (int) ((double) NativeImage.getR(i) * d0);
                int g = (int) ((double) NativeImage.getG(i) * d0);
                int b = (int) ((double) NativeImage.getB(i) * d0);
                int i1 = NativeImage.combine(0, b, g, r);

                int scalingfactor;

                List<IReorderingProcessor> tempPageLines;

                if (tile.getFlag()) {
                    ITextProperties txt = TextUtil.iGetPageText(page);
                    int width = fontrenderer.width(txt);
                    float bordery = 0.125f;
                    float borderx = 0.1875f;
                    float lx = 1 - (2 * borderx);
                    float ly = 1 - (2 * bordery);
                    float maxlines;
                    do {
                        scalingfactor = MathHelper.floor(MathHelper.sqrt((width * 8f) / (lx * ly)));

                        tempPageLines = fontrenderer.split(txt, MathHelper.floor(lx * scalingfactor));
                        //tempPageLines = RenderComponentsUtil.splitText(txt, MathHelper.floor(lx * scalingfactor), fontrenderer, true, true);

                        maxlines = ly * scalingfactor / 8f;
                        width += 1;
                        // when lines fully filled @scaling factor > actual lines -> no overflow lines
                        // rendered
                    } while (maxlines < tempPageLines.size());

                    tile.setFontScale(scalingfactor);
                    tile.setChachedPageLines(tempPageLines);
                } else {
                    tempPageLines = tile.getCachedPageLines();
                    scalingfactor = tile.getFontScale();
                }

                float scale = 1 / (float) scalingfactor;
                matrixStackIn.scale(scale, -scale, scale);
                int numberoflin = tempPageLines.size();

                for (int lin = 0; lin < numberoflin; ++lin) {
                    //String str = tempPageLines.get(lin).getFormattedText();
                    IReorderingProcessor str = tempPageLines.get(lin);

                    //border offsets. always add 0.5 to center properly
                    //float dx = (float) (-fontrenderer.getStringWidth(str) / 2f) + 0.5f;
                    float dx = (float) (-fontrenderer.width(str) / 2) + 0.5f;

                    // float dy = (float) scalingfactor * bordery;
                    float dy = ((scalingfactor - (8 * numberoflin)) / 2f) + 0.5f;

                    if(!bookName.equals("missingno")) {
                        fontrenderer.drawInBatch(str, dx, dy + 8 * lin, i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, frontLight);
                    }else {
                        fontrenderer.drawInBatch("\u00A7ka", dx, dy + 8 * lin, i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, frontLight);
                    }
                }
                matrixStackIn.popPose();
                matrixStackIn.popPose();
                return;

            }

            //render item
            if(!stack.isEmpty()){
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);

                matrixStackIn.pushPose();
                matrixStackIn.translate(0,0,0.015625+0.00005);
                matrixStackIn.scale(-0.5f, 0.5f, -0.5f);
                itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, frontLight,
                        combinedOverlayIn, ibakedmodel);
                //itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, newl, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);

                matrixStackIn.popPose();
                matrixStackIn.popPose();
                return;

            }
            matrixStackIn.popPose();
        }
    }

}