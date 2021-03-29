package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.HangingSignBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.Lod;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.RequestMapDataFromServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
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
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;


public class HangingSignBlockTileRenderer extends TileEntityRenderer<HangingSignBlockTile> {
    private static final int MAXLINES = 5;
    
    public HangingSignBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }


    @Override
    public void render(HangingSignBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        //rotate towards direction
        if(tile.getBlockState().getValue(HangingSignBlock.HANGING))matrixStackIn.translate(0,0.125, 0);
        matrixStackIn.translate(0.5, 0.875, 0.5);
        matrixStackIn.mulPose(tile.getDirection().getOpposite().getRotation());
        matrixStackIn.mulPose(Const.XN90);


        Lod lod = new Lod(this.renderer,tile.getBlockPos());

        //animation
        if(lod.isNear()) {
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle, tile.angle)));
        }
        matrixStackIn.translate(-0.5, -0.875, -0.5);
        //render block
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState state = tile.getBlockState().getBlock().defaultBlockState().setValue(HangingSignBlock.TILE, true);
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        //RendererUtil.renderBlockPlus(state, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos(), RenderType.getCutout());

        if(lod.isMedium()){
            matrixStackIn.translate(0.5, 0.5 - 0.1875, 0.5);
            matrixStackIn.mulPose(Const.YN90);
            // render item
            if (!tile.isEmpty()) {
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                ItemStack stack = tile.getStackInSlot(0);
                IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);

                MapData mapdata = FilledMapItem.getOrCreateSavedData(stack, tile.getLevel());

                for (int v = 0; v < 2; v++) {
                    matrixStackIn.pushPose();
                    //render map
                    if(stack.getItem() instanceof AbstractMapItem) {
                        if (mapdata != null) {
                            matrixStackIn.translate(0, 0, -0.0625 - 0.005);
                            matrixStackIn.scale(-0.0068359375F, -0.0068359375F, -0.0068359375F);
                            matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
                            //matrixStackIn.translate(0.0D, 0.0D, -1.0D);
                            Minecraft.getInstance().gameRenderer.getMapRenderer().render(matrixStackIn, bufferIn, mapdata, true, combinedLightIn);
                        }
                        else{
                            //request map data from server
                            PlayerEntity player = Minecraft.getInstance().player;
                            NetworkHandler.INSTANCE.sendToServer(new RequestMapDataFromServerPacket(tile.getBlockPos(),player.getUUID()));
                        }
                    }
                    //render item
                    else{
                        matrixStackIn.translate(0, 0, 0.078125);
                        matrixStackIn.scale(0.5f, 0.5f, 0.5f);
                        matrixStackIn.mulPose(Const.Y180);
                        itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                                combinedOverlayIn, ibakedmodel);
                    }
                    matrixStackIn.popPose();

                    matrixStackIn.mulPose(Const.Y180);

                }
            }
            // render text
            else if(lod.isNear()){
                // sign code
                FontRenderer fontrenderer = this.renderer.getFont();
                int i = tile.textHolder.textColor.getTextColor();
                int j = (int) ((double) NativeImage.getR(i) * 0.4D);
                int k = (int) ((double) NativeImage.getG(i) * 0.4D);
                int l = (int) ((double) NativeImage.getB(i) * 0.4D);
                int i1 = NativeImage.combine(0, l, k, j);

                for (int v = 0; v < 2; v++) {
                    matrixStackIn.pushPose();
                    matrixStackIn.translate(0, 0, 0.0625 + 0.005);
                    matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);

                    for(int k1 = 0; k1 < MAXLINES; ++k1) {
                        IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(k1, (p_243502_1_) -> {
                            List<IReorderingProcessor> list = fontrenderer.split(p_243502_1_, 75);
                            return list.isEmpty() ? IReorderingProcessor.EMPTY : list.get(0);
                        });
                        if (ireorderingprocessor != null) {
                            float f3 = (float)(-fontrenderer.width(ireorderingprocessor) / 2);
                            fontrenderer.drawInBatch(ireorderingprocessor, f3, (float)(k1 * 10 - 20), i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);
                        }
                    }


                    matrixStackIn.popPose();
                    matrixStackIn.mulPose(Const.Y180);
                }
            }
        }
        matrixStackIn.popPose();
    }
}