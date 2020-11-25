package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.blocks.HangingSignBlockTile;
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
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class HangingSignBlockTileRenderer extends TileEntityRenderer<HangingSignBlockTile> {
    private static final int MAXLINES = 5;
    public HangingSignBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(HangingSignBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.push();
        //rotate towards direction
        if(tile.getBlockState().get(HangingSignBlock.UP))matrixStackIn.translate(0,0.125, 0);
        matrixStackIn.translate(0.5, 0.875, 0.5);
        matrixStackIn.rotate(tile.getDirection().getOpposite().getRotation());
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));

        //animation
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle, tile.angle)));
        matrixStackIn.translate(-0.5, -0.875, -0.5);
        //render block
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = tile.getBlockState().getBlock().getDefaultState().with(HangingSignBlock.TILE, true);
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.translate(0.5, 0.5 - 0.1875, 0.5);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
        // render item
        if (!tile.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack stack = tile.getStackInSlot(0);
            IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, tile.getWorld(), null);

            MapData mapdata = FilledMapItem.getMapData(stack, tile.getWorld());
            for (int v = 0; v < 2; v++) {
                matrixStackIn.push();
                //render map
                if(mapdata != null){
                    matrixStackIn.translate(0, 0, -0.0625 - 0.005);
                    matrixStackIn.scale(-0.0068359375F, -0.0068359375F, -0.0068359375F);
                    matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
                    //matrixStackIn.translate(0.0D, 0.0D, -1.0D);
                    Minecraft.getInstance().gameRenderer.getMapItemRenderer().renderMap(matrixStackIn, bufferIn, mapdata, true, combinedLightIn);
                }
                //render item
                else{
                    matrixStackIn.translate(0, 0, 0.078125);
                    matrixStackIn.scale(0.5f, 0.5f, 0.5f);
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
                    itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                            combinedOverlayIn, ibakedmodel);
                }
                matrixStackIn.pop();

                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));

            }
        }
        // render text
        else {
            // sign code
            FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
            int i = tile.getTextColor().getTextColor();
            int j = (int) ((double) NativeImage.getRed(i) * 0.4D);
            int k = (int) ((double) NativeImage.getGreen(i) * 0.4D);
            int l = (int) ((double) NativeImage.getBlue(i) * 0.4D);
            int i1 = NativeImage.getCombined(0, l, k, j);

            for (int v = 0; v < 2; v++) {
                matrixStackIn.push();
                matrixStackIn.translate(0, 0, 0.0625 + 0.005);
                matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);

                for(int k1 = 0; k1 < MAXLINES; ++k1) {
                    IReorderingProcessor ireorderingprocessor = tile.getRenderText(k1, (p_243502_1_) -> {
                        List<IReorderingProcessor> list = fontrenderer.trimStringToWidth(p_243502_1_, 75);
                        return list.isEmpty() ? IReorderingProcessor.field_242232_a : list.get(0);
                    });
                    if (ireorderingprocessor != null) {
                        float f3 = (float)(-fontrenderer.func_243245_a(ireorderingprocessor) / 2);
                        fontrenderer.func_238416_a_(ireorderingprocessor, f3, (float)(k1 * 10 - 20), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
                    }
                }


                matrixStackIn.pop();
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
            }
        }
        matrixStackIn.pop();
    }
}