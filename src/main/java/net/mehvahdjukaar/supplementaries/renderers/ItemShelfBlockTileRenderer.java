package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.tiles.ItemShelfBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemShelfBlockTileRenderer extends TileEntityRenderer<ItemShelfBlockTile> {
    public ItemShelfBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    protected boolean canRenderName(ItemShelfBlockTile tile) {
        if (Minecraft.isGuiEnabled() && tile.getStackInSlot(0).hasDisplayName()) {
            double d0 = Minecraft.getInstance().getRenderManager().getDistanceToCamera(tile.getPos().getX() + 0.5 ,tile.getPos().getY() + 0.5 ,tile.getPos().getZ() + 0.5);
            return d0 < 16;
        }
        return false;
    }

    protected void renderName(ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

        double f = 0.625; //height
        int i = 0;

        FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
        EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();

        matrixStackIn.push();

        matrixStackIn.translate(0, f, 0);
        matrixStackIn.rotate(renderManager.getCameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
        float f1 = Minecraft.getInstance().gameSettings.getTextBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;

        float f2 = (float)(-fontrenderer.getStringPropertyWidth(displayNameIn) / 2);
        //func_243247_a == renderTextComponent
        fontrenderer.func_243247_a(displayNameIn, f2, (float)i, -1, false, matrix4f, bufferIn, false, j, packedLightIn);
        matrixStackIn.pop();

    }


    @Override
    public void render(ItemShelfBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if(!tile.isEmpty()){

            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            float yaw = tile.getYaw();
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(yaw));
            matrixStackIn.translate(0,0,0.8125);

            if(this.canRenderName(tile)){
                matrixStackIn.push();
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-yaw));
                ITextComponent name = tile.getStackInSlot(0).getDisplayName();
                int i = "Dinnerbone".equals(name.getString())? -1 : 1;
                matrixStackIn.scale(i, i, 1);
                this.renderName(name, matrixStackIn, bufferIn, combinedLightIn);
                matrixStackIn.pop();
            }




            //TODO: make all blocks and tiles extend a basic block with all the boilerplate in it


            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack stack = tile.getStackInSlot(0);
            if(CommonUtil.aprilfool)stack= new ItemStack(Items.DIAMOND);
            IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, tile.getWorld(), null);
            if(ibakedmodel.isGui3d()&&ClientConfigs.cached.SHELF_TRANSLATE)matrixStackIn.translate(0,-0.25,0);
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                    combinedOverlayIn, ibakedmodel);

            matrixStackIn.pop();
        }
    }
}