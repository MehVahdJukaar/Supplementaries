package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.TextUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.RequestMapDataFromServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AbstractMapItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import java.util.List;


public class NoticeBoardBlockTileRenderer extends TileEntityRenderer<NoticeBoardBlockTile> {
    protected final ItemRenderer itemRenderer;
    protected final MapItemRenderer mapRenderer;
    public NoticeBoardBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        Minecraft minecraft = Minecraft.getInstance();
        itemRenderer = minecraft.getItemRenderer();
        mapRenderer = minecraft.gameRenderer.getMapRenderer();
    }

    public int getFrontLight(World world, BlockPos pos, Direction dir) {
        return WorldRenderer.getLightColor(world, pos.relative(dir));
    }

    public boolean getAxis(Direction dir) {
        return dir == Direction.NORTH || dir == Direction.SOUTH;
    }

    @Override
    public void render(NoticeBoardBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        //TODO: rewrite
        if(!tile.shouldSkipTileRenderer()){

            ItemStack stack = tile.getDisplayedItem();

            if(stack.isEmpty())return;

            World world = tile.getLevel();
            Direction dir = tile.getDirection();

            float yaw = -dir.toYRot();
            Vector3d cameraPos = this.renderer.camera.getPosition();
            BlockPos pos = tile.getBlockPos();
            if(LOD.isOutOfFocus(cameraPos, pos, yaw))return;

            //TODO: fix book with nothing in it
            int frontLight = this.getFrontLight(world, pos, dir);

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.mulPose(Const.rot((int)yaw));
            matrixStackIn.translate(0, 0, 0.5);

            //render map
            MapData mapdata = FilledMapItem.getOrCreateSavedData(stack, world);
            if(stack.getItem() instanceof AbstractMapItem) {
                if (mapdata != null) {
                    matrixStackIn.pushPose();
                    matrixStackIn.translate(0, 0, 0.008);
                    matrixStackIn.scale(0.0078125F, -0.0078125F, -0.0078125F);
                    matrixStackIn.translate(-64.0D, -64.0D, 0.0D);

                    mapRenderer.render(matrixStackIn, bufferIn, mapdata, true, frontLight);
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

                LOD lod = new LOD(cameraPos,pos);
                if(!lod.isNearMed()) {
                    matrixStackIn.popPose();
                    return;
                }

                FontRenderer fontrenderer = this.renderer.getFont();

                matrixStackIn.pushPose();
                matrixStackIn.translate(0,0.5,0.008);

                float d0;
                if (this.getAxis(dir)) {
                    d0 = 0.8f * 0.7f;
                } else {
                    d0 = 0.6f * 0.7f;
                }


                if(CommonUtil.FESTIVITY.isAprilsFool()){
                    TextUtil.renderBeeMovie(matrixStackIn,bufferIn,frontLight,fontrenderer,d0);
                    matrixStackIn.popPose();
                    matrixStackIn.popPose();
                    return;
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
                    tile.setCachedPageLines(tempPageLines);
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
            if(!stack.isEmpty()) {

                RenderMaterial rendermaterial = tile.getCachedPattern();
                if (rendermaterial != null) {

                    IVertexBuilder builder = rendermaterial.buffer(bufferIn, RenderType::entityNoOutline);

                    int i = tile.getTextColor().getTextColor();
                    float b = (NativeImage.getR(i) )/255f;
                    float g = (NativeImage.getG(i) )/255f;
                    float r = (NativeImage.getB(i) )/255f;
                    int lu = frontLight & '\uffff';
                    int lv = frontLight >> 16 & '\uffff';
                    RendererUtil.addQuadSide(builder, matrixStackIn, -0.4375F, -0.4375F, 0.008f, 0.4375F, 0.4375F, 0.008f,
                            0.15625f, 0.0625f, 0.5f + 0.09375f, 1 - 0.0625f, r, g, b, 1, lu, lv, 0, 0, 1, rendermaterial.sprite());

                }
                else{
                    IBakedModel ibakedmodel = itemRenderer.getModel(stack, world, null);

                    matrixStackIn.translate(0, 0, 0.015625 + 0.00005);
                    matrixStackIn.scale(-0.5f, 0.5f, -0.5f);
                    itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, frontLight,
                            combinedOverlayIn, ibakedmodel);
                    //itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, newl, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
                }

                matrixStackIn.popPose();
                return;

            }
            matrixStackIn.popPose();
        }
    }

}