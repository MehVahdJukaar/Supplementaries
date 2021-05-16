package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.tiles.ItemShelfBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;


public class ItemShelfBlockTileRenderer extends TileEntityRenderer<ItemShelfBlockTile> {
    protected final ItemRenderer itemRenderer;
    public ItemShelfBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    protected boolean canRenderName(ItemShelfBlockTile tile) {
        if (Minecraft.renderNames() && tile.getItem(0).hasCustomHoverName()) {
            double d0 = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(tile.getBlockPos().getX() + 0.5 ,tile.getBlockPos().getY() + 0.5 ,tile.getBlockPos().getZ() + 0.5);
            return d0 < 16;
        }
        return false;
    }

    protected void renderName(ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

        double f = 0.625; //height
        int i = 0;

        FontRenderer fontrenderer = this.renderer.getFont();
        EntityRendererManager renderManager = Minecraft.getInstance().getEntityRenderDispatcher();

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, f, 0);
        matrixStackIn.mulPose(renderManager.cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStackIn.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;

        float f2 = (float)(-fontrenderer.width(displayNameIn) / 2);
        //drawInBatch == renderTextComponent
        fontrenderer.drawInBatch(displayNameIn, f2, (float)i, -1, false, matrix4f, bufferIn, false, j, packedLightIn);
        matrixStackIn.popPose();

    }


    @Override
    public void render(ItemShelfBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if(!tile.isEmpty()){

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            float yaw = tile.getYaw();
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(yaw));
            matrixStackIn.translate(0,0,0.8125);

            if(this.canRenderName(tile)){
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-yaw));
                ITextComponent name = tile.getItem(0).getHoverName();
                int i = "Dinnerbone".equals(name.getString())? -1 : 1;
                matrixStackIn.scale(i, i, 1);
                this.renderName(name, matrixStackIn, bufferIn, combinedLightIn);
                matrixStackIn.popPose();
            }

            ItemStack stack = tile.getItem(0);
            if(CommonUtil.FESTIVITY.isAprilsFool())stack= new ItemStack(Items.SALMON);
            IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);
            if(ibakedmodel.isGui3d()&&ClientConfigs.cached.SHELF_TRANSLATE)matrixStackIn.translate(0,-0.25,0);


            itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                    combinedOverlayIn, ibakedmodel);


            matrixStackIn.popPose();
        }

    }

}