package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.HangingSignBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.RequestMapDataFromServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;


public class HangingSignBlockTileRenderer extends TileEntityRenderer<HangingSignBlockTile> {
    protected final BlockRendererDispatcher blockRenderer;
    protected final ItemRenderer itemRenderer;
    protected final MapItemRenderer mapRenderer;

    public HangingSignBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        Minecraft minecraft = Minecraft.getInstance();
        blockRenderer = minecraft.getBlockRenderer();
        itemRenderer = minecraft.getItemRenderer();
        mapRenderer = minecraft.gameRenderer.getMapRenderer();
    }


    @Override
    public void render(HangingSignBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();

        //rotate towards direction
        if (tile.getBlockState().getValue(HangingSignBlock.HANGING)) matrixStackIn.translate(0, 0.125, 0);
        matrixStackIn.translate(0.5, 0.875, 0.5);
        matrixStackIn.mulPose(Const.rot(tile.getDirection().getOpposite()));
        matrixStackIn.mulPose(Const.XN90);

        LOD lod = new LOD(this.renderer, tile.getBlockPos());

        //animation
        if (lod.isNear()) {
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle, tile.angle)));
        }
        matrixStackIn.translate(-0.5, -0.875, -0.5);
        //render block
        BlockState state = tile.getBlockState().getBlock().defaultBlockState().setValue(HangingSignBlock.TILE, true);

        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        //RendererUtil.renderBlockPlus(state, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos(), RenderType.getCutout());

        if (lod.isMedium()) {
            matrixStackIn.translate(0.5, 0.5 - 0.1875, 0.5);
            matrixStackIn.mulPose(Const.YN90);
            // render item
            if (!tile.isEmpty()) {
                ItemStack stack = tile.getStackInSlot(0);
                Item item = stack.getItem();

                //render map
                if (item instanceof AbstractMapItem) {
                    MapData mapdata = FilledMapItem.getOrCreateSavedData(stack, tile.getLevel());
                    if (mapdata != null) {
                        for (int v = 0; v < 2; v++) {
                            matrixStackIn.pushPose();
                            matrixStackIn.translate(0, 0, 0.0625 + 0.005);
                            matrixStackIn.scale(0.0068359375F, -0.0068359375F, -0.0068359375F);
                            matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
                            //matrixStackIn.translate(0.0D, 0.0D, -1.0D);
                            mapRenderer.render(matrixStackIn, bufferIn, mapdata, true, combinedLightIn);
                            matrixStackIn.popPose();

                            matrixStackIn.mulPose(Const.Y180);
                        }
                    } else {
                        //request map data from server
                        PlayerEntity player = Minecraft.getInstance().player;
                        NetworkHandler.INSTANCE.sendToServer(new RequestMapDataFromServerPacket(tile.getBlockPos(), player.getUUID()));
                    }
                } else if (item instanceof BannerPatternItem) {

                    //TODO: cache or not like notice board
                    RenderMaterial rendermaterial = Materials.FLAG_MATERIALS.get(((BannerPatternItem) item).getBannerPattern());

                    IVertexBuilder builder = rendermaterial.buffer(bufferIn, RenderType::entityNoOutline);

                    //IVertexBuilder builder = bufferIn.getBuffer(RenderType.itemEntityTranslucentCull(FlagBlockTile.getFlagLocation(((BannerPatternItem) item).getBannerPattern())));

                    int i = tile.textHolder.textColor.getColorValue();
                    float b = (NativeImage.getR(i)) / 255f;
                    float g = (NativeImage.getG(i)) / 255f;
                    float r = (NativeImage.getB(i)) / 255f;
                    int lu = combinedLightIn & '\uffff';
                    int lv = combinedLightIn >> 16 & '\uffff';
                    for (int v = 0; v < 2; v++) {
                        RendererUtil.addQuadSide(builder, matrixStackIn, -0.4375F, -0.4375F, 0.0725f, 0.4375F, 0.4375F, 0.0725f,
                                0.15625f, 0.0625f, 0.5f + 0.09375f, 1 - 0.0625f, r, g, b, 1, lu, lv, 0, 0, 1, rendermaterial.sprite());

                        matrixStackIn.mulPose(Const.Y180);
                    }
                }
                //render item
                else {
                    IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);
                    for (int v = 0; v < 2; v++) {
                        matrixStackIn.pushPose();

                        matrixStackIn.scale(0.75f, 0.75f, 0.75f);
                        matrixStackIn.translate(0, 0, -1);
                        //matrixStackIn.mulPose(Const.Y180);
                        itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                                combinedOverlayIn, ibakedmodel);
                        matrixStackIn.popPose();

                        matrixStackIn.mulPose(Const.Y180);
                        matrixStackIn.scale(0.9995f, 0.9995f, 0.9995f);
                    }
                }

            }

            // render text
            else if (lod.isNearMed()) {
                // sign code
                FontRenderer fontrenderer = this.renderer.getFont();
                int i = tile.textHolder.textColor.getTextColor();
                int j = (int) ((double) NativeImage.getR(i) * 0.4D);
                int k = (int) ((double) NativeImage.getG(i) * 0.4D);
                int l = (int) ((double) NativeImage.getB(i) * 0.4D);
                int i1 = NativeImage.combine(0, l, k, j);


                matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);

                for (int k1 = 0; k1 < HangingSignBlockTile.MAXLINES; ++k1) {
                    IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(k1, (ss) -> {
                        List<IReorderingProcessor> list = fontrenderer.split(ss, 75);
                        return list.isEmpty() ? IReorderingProcessor.EMPTY : list.get(0);
                    });
                    if (ireorderingprocessor != null) {

                        for (int v = 0; v < 2; v++) {

                            matrixStackIn.pushPose();
                            matrixStackIn.translate(0, 0, (0.0625 + 0.005) / 0.010416667F);

                            float f3 = (float) (-fontrenderer.width(ireorderingprocessor) / 2);
                            fontrenderer.drawInBatch(ireorderingprocessor, f3, (float) (k1 * 10 - 34), i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);

                            matrixStackIn.popPose();

                            matrixStackIn.mulPose(Const.Y180);
                        }
                    }
                }
            }

        }
        matrixStackIn.popPose();
    }
}