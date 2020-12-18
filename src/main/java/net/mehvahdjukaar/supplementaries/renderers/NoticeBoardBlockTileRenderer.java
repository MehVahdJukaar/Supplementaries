package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.tiles.NoticeBoardBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NoticeBoardBlockTileRenderer extends TileEntityRenderer<NoticeBoardBlockTile> {

    public NoticeBoardBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    public ITextProperties iGetPageText(String s) {
        try {
            ITextProperties itextproperties = ITextComponent.Serializer.getComponentFromJson(s);
            if (itextproperties != null) {
                return itextproperties;
            }
        } catch (Exception ignored) {
        }
        return ITextProperties.func_240652_a_(s);
    }

    @Override
    public void render(NoticeBoardBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if(tile.textVisible){

            int newl = tile.getFrontLight();
            ItemStack stack = tile.getStackInSlot(0);

            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(tile.getYaw()));
            matrixStackIn.translate(0, 0, 0.5);

            //render map

            MapData mapdata = FilledMapItem.getData(stack, tile.getWorld());
            //MapData mapdata = tile.mapData;
            if(mapdata != null){
                matrixStackIn.push();
                matrixStackIn.translate(0,0,0.008);
                matrixStackIn.scale(0.0078125F, -0.0078125F, -0.0078125F);
                matrixStackIn.translate(-64.0D, -64.0D, 0.0D);

                Minecraft.getInstance().gameRenderer.getMapItemRenderer().renderMap(matrixStackIn, bufferIn, mapdata, true, newl);
                matrixStackIn.pop();
                matrixStackIn.pop();
                return;
            }

            //render book
            String page = tile.getText();
            if (!(page == null || page.equals(""))) {

                FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();

                matrixStackIn.push();
                matrixStackIn.translate(0,0.5,0.008);

                float d0;
                if (tile.getAxis()) {
                    d0 = 0.8f * 0.7f;
                } else {
                    d0 = 0.6f * 0.7f;
                }

                int i = tile.getTextColor().getTextColor();
                int r = (int) ((double) NativeImage.getRed(i) * d0);
                int g = (int) ((double) NativeImage.getGreen(i) * d0);
                int b = (int) ((double) NativeImage.getBlue(i) * d0);
                int i1 = NativeImage.getCombined(0, b, g, r);

                float bordery = 0.125f;
                float borderx = 0.1875f;
                int scalingfactor;

                //List<ITextComponent> tempPageLines;
                List<IReorderingProcessor> tempPageLines;

                ITextProperties txt = iGetPageText(page);
                //ITextComponent txt = new StringTextComponent(page);

                //int width = fontrenderer.getStringWidth(txt.getFormattedText());
                int width = fontrenderer.getStringPropertyWidth(txt);

                if (tile.getFlag()) {
                    float lx = 1 - (2 * borderx);
                    float ly = 1 - (2 * bordery);
                    float maxlines;
                    do {
                        scalingfactor = MathHelper.floor(MathHelper.sqrt((width * 8f) / (lx * ly)));

                        tempPageLines = fontrenderer.trimStringToWidth(txt, MathHelper.floor(lx * scalingfactor));
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
                    float dx = (float) (-fontrenderer.func_243245_a(str) / 2) + 0.5f;

                    // float dy = (float) scalingfactor * bordery;
                    float dy = ((scalingfactor - (8 * numberoflin)) / 2f) + 0.5f;


                    fontrenderer.func_238416_a_(str, dx, dy + 8 * lin, i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, newl);
                    // fontrenderer.renderString(str, dx, dy + 8 * lin, i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, newl);
                }
                matrixStackIn.pop();
                matrixStackIn.pop();
                return;

            }

            //render item
            if(!stack.isEmpty()){
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, tile.getWorld(), null);

                matrixStackIn.push();
                matrixStackIn.translate(0,0,0.015625+0.00005);
                matrixStackIn.scale(-0.5f, 0.5f, -0.5f);
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, newl,
                        combinedOverlayIn, ibakedmodel);
                //itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, newl, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);

                matrixStackIn.pop();
                matrixStackIn.pop();
                return;

            }
            matrixStackIn.pop();
        }
    }
}