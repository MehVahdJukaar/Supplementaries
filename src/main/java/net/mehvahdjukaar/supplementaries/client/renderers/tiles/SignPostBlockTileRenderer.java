package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.client.Textures;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.WoodType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;


public class SignPostBlockTileRenderer extends TileEntityRenderer<SignPostBlockTile> {
    private final ModelRenderer signModel = new ModelRenderer(64, 16, 0, 0);
    public static final RenderMaterial MATERIAL = new RenderMaterial(Atlases.SIGN_ATLAS, Textures.SIGN_POSTS_TEXTURES.get(VanillaWoodTypes.SPRUCE));


    public SignPostBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        this.signModel.setRotationPoint(-8.0F, 20.0F, -3.0F);
        this.signModel.setTextureOffset(0, 10).addBox(-4.0F, 1.0F, 0.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        this.signModel.setTextureOffset(0, 0).addBox(0.0F, -1.0F, 0.0F, 16.0F, 5.0F, 1.0F, 0.0F, false);
        this.signModel.setTextureOffset(0, 6).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 3.0F, 1.0F, 0.0F, false);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }


    @Override
    public void render(SignPostBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

       // IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Resources.SignPostsTextures.OAK));
        IVertexBuilder builder =  MATERIAL.getBuffer(bufferIn, RenderType::getEntitySolid);
        //IVertexBuilder builder = Resources.SignPostsTextures.OAK_MATERIAL.getBuffer(bufferIn, RenderType::getEntitySolid);
        this.signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

        BlockState fence = tile.fenceBlock;
        if(fence !=null){
            RendererUtil.renderBlockPlus(fence, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos(),  RenderType.getCutout());
            //blockRenderer.renderBlock(fence, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        }

        boolean up = tile.up;
        boolean down = tile.down;
        //render signs
        if(up||down){


            // sign code
            FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
            int i = tile.textHolder.textColor.getTextColor();
            int j = (int) ((double) NativeImage.getRed(i) * 0.4D);
            int k = (int) ((double) NativeImage.getGreen(i) * 0.4D);
            int l = (int) ((double) NativeImage.getBlue(i) * 0.4D);
            int i1 = NativeImage.getCombined(0, l, k, j);

            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.5, 0.5);

            if(up){
                matrixStackIn.push();

                BlockState state = Registry.SIGN_POST.get().getDefaultState().with(SignPostBlock.WOOD_TYPE, tile.woodTypeUp);

                boolean left = tile.leftUp;
                int o = left ? 1 : -1;

                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(tile.yawUp));
                //sign block
                matrixStackIn.push();

                if(!left){
                    matrixStackIn.translate(-0.15625, 0, 0);
                    matrixStackIn.rotate(Const.YN180);
                    matrixStackIn.translate(0.15625, 0, 0);
                }
                matrixStackIn.translate(-0.5, -0.5, -0.5);
                blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

                matrixStackIn.pop();


                //text up
                matrixStackIn.rotate(Const.YN90);
                matrixStackIn.translate(-0.03125*o, 0.28125, 0.1875 + 0.005);
                matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
                matrixStackIn.translate(0, 1, 0);


                    IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(0, (p_243502_1_) -> {
                        List<IReorderingProcessor> list = fontrenderer.trimStringToWidth(p_243502_1_, 90);
                        return list.isEmpty() ? IReorderingProcessor.field_242232_a : list.get(0);
                    });
                    if (ireorderingprocessor != null) {
                        float f3 = (float)(-fontrenderer.func_243245_a(ireorderingprocessor) / 2);
                        fontrenderer.func_238416_a_(ireorderingprocessor, f3, (float)(-5), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
                    }

                matrixStackIn.pop();
            }
            if(down){
                matrixStackIn.push();

                BlockState state = Registry.SIGN_POST.get().getDefaultState().with(SignPostBlock.WOOD_TYPE, tile.woodTypeDown);

                boolean left = tile.leftDown;
                int o = left ? 1 : -1;

                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(tile.yawDown));
                //sign block
                matrixStackIn.push();

                if(!left){
                    matrixStackIn.translate(-0.15625, 0, 0);
                    matrixStackIn.rotate(Const.YN180);
                    matrixStackIn.translate(0.15625, 0, 0);
                }
                matrixStackIn.translate(-0.5, -1, -0.5);
                blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
                matrixStackIn.pop();

                //text down
                matrixStackIn.translate(0, -0.5, 0);
                matrixStackIn.rotate(Const.YN90);
                matrixStackIn.translate(-0.03125*o, 0.28125, 0.1875 + 0.005);
                matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
                matrixStackIn.translate(0, 1, 0);

                IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(1, (p_243502_1_) -> {
                    List<IReorderingProcessor> list = fontrenderer.trimStringToWidth(p_243502_1_, 90);
                    return list.isEmpty() ? IReorderingProcessor.field_242232_a : list.get(0);
                });
                if (ireorderingprocessor != null) {
                    float f3 = (float)(-fontrenderer.func_243245_a(ireorderingprocessor) / 2);
                    fontrenderer.func_238416_a_(ireorderingprocessor, f3, (float)(-5), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
                }


                matrixStackIn.pop();
            }
            matrixStackIn.pop();
        }

    }
}
