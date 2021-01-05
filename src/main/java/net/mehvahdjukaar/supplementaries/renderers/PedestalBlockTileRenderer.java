package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.tiles.PedestalBlockTile;
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

        double f = 0.875; //height
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

        if(!tile.isEmpty()){
            //TODO: optimize transforms
            matrixStackIn.push();
            matrixStackIn.translate(0.5, 1.125, 0.5);

            if(this.canRenderName(tile)){
                ITextComponent name = tile.getStackInSlot(0).getDisplayName();
                int i = "Dinnerbone".equals(name.getString())? -1 : 1;
                matrixStackIn.scale(i, i, 1);
                this.renderName(name, matrixStackIn, bufferIn, combinedLightIn);
            }
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            matrixStackIn.translate(0, 0.25, 0);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(tile.yaw));

            ItemCameraTransforms.TransformType transform = ItemCameraTransforms.TransformType.FIXED;
            if(tile.type==2 && ClientConfigs.cached.PEDESTAL_SWORD){
                //sword
                //matrixStackIn.translate(0,-0.03125,0);
                matrixStackIn.translate(0,-0.03125,0);
                matrixStackIn.scale(1.5f,1.5f,1.5f);


                matrixStackIn.rotate(Const.Z135);

            }
            else if(tile.type==4){
                //transform = ItemCameraTransforms.TransformType.HEAD;
                //matrixStackIn.scale(1.75f,1.75f,1.75f);
                matrixStackIn.translate(0,0.03125,0);
                matrixStackIn.scale(1.5f,1.5f,1.5f);

                matrixStackIn.rotate(Const.ZN45);

            }
            else {
                if (ClientConfigs.cached.PEDESTAL_SPIN && !Minecraft.getInstance().isGamePaused()) {
                    matrixStackIn.translate(0,6/16f, 0);
                    matrixStackIn.scale(1.5f,1.5f,1.5f);

                    //BlockPos blockpos = tile.getPos();
                    //long blockoffset = (long) (blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13);

                    //long time = System.currentTimeMillis();
                    //TODO: fix stuttering
                    //float tt = tile.getWorld().getGameTime()+ partialTicks;
                    float tt = tile.counter+partialTicks;


                    //long t = blockoffset + time;
                    float angle = (tt * (float)ClientConfigs.cached.PEDESTAL_SPEED ) % 360f;
                    Quaternion rotation = Vector3f.YP.rotationDegrees(angle);

                    matrixStackIn.rotate(rotation);
                }

            }
            //TODO: make FIXED
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack stack = tile.getStackInSlot(0);
            if(CommonUtil.aprilfool)stack= new ItemStack(Items.DIRT);
            IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, tile.getWorld(), null);
            itemRenderer.renderItem(stack, transform, true, matrixStackIn, bufferIn, combinedLightIn,
                    combinedOverlayIn, ibakedmodel);

            matrixStackIn.pop();
        }
    }
}