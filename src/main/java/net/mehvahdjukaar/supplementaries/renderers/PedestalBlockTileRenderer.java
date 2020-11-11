package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.PedestalBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PedestalBlockTileRenderer extends TileEntityRenderer<PedestalBlockTile> {
    public PedestalBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    protected boolean canRenderName(PedestalBlockTile tile) {
        if (Minecraft.isGuiEnabled() && tile.getStackInSlot(0).hasDisplayName()) {
            double d0 = Minecraft.getInstance().getRenderManager().getDistanceToCamera(tile.getPos().getX() + 0.5 ,tile.getPos().getY() + 0.5 ,tile.getPos().getZ() + 0.5);
            return d0 < 16*16;
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
    public void render(PedestalBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        /*
                long time = System.currentTimeMillis();
                float angle = (time / 40) % 360;
                Quaternion rotation = Vector3f.ZP.rotationDegrees(angle);
                matrixStackIn.scale(0.55f,0.55f,0.55f);
                matrixStackIn.translate(0.5/1.25, 1.125/1.25, 0.5/1.25);
                //matrixStackIn.translate(2*0.015625, 0, 0);
                matrixStackIn.rotate(rotation);
                matrixStackIn.translate(0.09375/1.25, 0, 0);

        switch((int)tile.getItemType()){
            case 1:
                long time = System.currentTimeMillis();
                float angle = (time / 40) % 360;
                Quaternion rotation = Vector3f.YP.rotationDegrees(angle);

                //matrixStackIn.translate(0.5, 1.1875, 0.5);
                matrixStackIn.translate(0.5, 1.125, 0.5);

                //matrixStackIn.scale(0.5f,0.5f,0.5f);
                //matrixStackIn.rotate(rotation);
                break;
            case 2:
                matrixStackIn.translate(0.25, 0.25, 0.5);
                Quaternion rotation1 = Vector3f.ZP.rotationDegrees(45);
                matrixStackIn.rotate(rotation1);matrixStackIn.translate(0D, 0.5, 0.D);
                //matrixStackIn.scale(1.25f,1.25f,1.25f);
                break;
            case 0:
                matrixStackIn.translate(0.5, 0.2, 0.5);
                break;
            case 3:
                matrixStackIn.translate(0.5, 1.125, 0.5);
                break;
        }*/

        if(!tile.isEmpty()){

            matrixStackIn.push();
            matrixStackIn.translate(0.5, 1.125, 0.5);

            if(this.canRenderName(tile)){
                ITextComponent name = tile.getStackInSlot(0).getDisplayName();
                int i = "Dinnerbone".equals(name.getString())? -1 : 1;
                matrixStackIn.scale(i, i, 1);
                this.renderName(name, matrixStackIn, bufferIn, combinedLightIn);
            }

            if(tile.type==2){
                matrixStackIn.scale(1.5f,1.5f,1.5f);
                matrixStackIn.translate(0,0.25,0);
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(tile.yaw));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-135));

                matrixStackIn.translate(0.125,0,0);

                //matrixStackIn.translate(-0.5, 0, 0);

            }
            else {
                if (!Minecraft.getInstance().isGamePaused()) {
                    BlockPos blockpos = tile.getPos();
                    long blockoffset = (long) (blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13);

                    long time = System.currentTimeMillis();
                    long t = blockoffset + time;
                    float angle = (t / 40) % 360;
                    Quaternion rotation = Vector3f.YP.rotationDegrees(angle);

                    matrixStackIn.rotate(rotation);
                }

            }
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack stack = tile.getStackInSlot(0);
            IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, tile.getWorld(), null);
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND, true, matrixStackIn, bufferIn, combinedLightIn,
                    combinedOverlayIn, ibakedmodel);

            matrixStackIn.pop();
        }
    }
}