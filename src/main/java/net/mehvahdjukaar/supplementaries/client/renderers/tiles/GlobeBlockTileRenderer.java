package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.mehvahdjukaar.supplementaries.world.data.GlobeDataGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class GlobeBlockTileRenderer extends TileEntityRenderer<GlobeBlockTile> {

    public static final ModelRenderer globe = new ModelRenderer(32, 16, 0, 0);
    public static final ModelRenderer flat = new ModelRenderer(32, 32, 0, 0);
    public static final ModelRenderer sheared = new ModelRenderer(32, 32, 0, 0);
    static {
        globe.addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        globe.setPos(0.0F, 24.0F, 0.0F);

        flat.setPos(0.0F, 24.0F, 0.0F);
        flat.texOffs(0, 0).addBox(-4.0F, -28.0F, -4.0F, 8.0F, 4.0F, 8.0F, 0.0F, false);
        flat.texOffs(0, 13).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 2.0F, 8.0F, 0.0F, false);
        flat.texOffs(4, 23).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
        flat.texOffs(8, 24).addBox(-2.0F, -21.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);

        sheared.setPos(0.0F, 24.0F, 0.0F);
        sheared.texOffs(0, 0).addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 4.0F, 0.0F, false);
        sheared.texOffs(0, 12).addBox(0.0F, -28.0F, 0.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
    }


    public GlobeBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

    }

    public void renderEarth(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int combinedLightIn, int combinedOverlayIn){
        matrixStack.mulPose(Const.X180);
        globe.render(matrixStack, vertexBuilder, combinedLightIn,combinedOverlayIn,1,1,1,1);
    }

    public void renderFlat(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int combinedLightIn, int combinedOverlayIn){
        matrixStack.mulPose(Const.X180);
        flat.render(matrixStack, vertexBuilder, combinedLightIn,combinedOverlayIn,1,1,1,1);
    }

    public void renderSheared(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int combinedLightIn, int combinedOverlayIn){
        matrixStack.mulPose(Const.X180);
        sheared.render(matrixStack, vertexBuilder, combinedLightIn,combinedOverlayIn,1,1,1,1);
    }

    @Override
    public void render(GlobeBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        //TODO: maybe convert into an int(might be faster for map access idk)
        String dimension = tile.getLevel().dimension().location().toString();

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5,0.5,0.5);
        matrixStackIn.mulPose(tile.getDirection().getRotation());
        matrixStackIn.mulPose(Const.XN90);
        matrixStackIn.translate(0,+0.0625,0);
        matrixStackIn.mulPose(Const.XN22);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevYaw+tile.face, tile.yaw+tile.face)));

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(tile.type.texture));

        if(!ClientConfigs.cached.GLOBE_RANDOM){
            this.renderEarth(matrixStackIn,builder,combinedLightIn,combinedOverlayIn);
        }

        //special cases for random generated, sheared and flat. custom ones are in earth
        switch(tile.type){
            case FLAT:
                this.renderFlat(matrixStackIn,builder,combinedLightIn,combinedOverlayIn);
                break;
            default:
            case EARTH:
                this.renderEarth(matrixStackIn,builder,combinedLightIn,combinedOverlayIn);
                break;
            case SHEARED:
                this.renderSheared(matrixStackIn,builder,combinedLightIn,combinedOverlayIn);
                break;
            case DEFAULT:
                matrixStackIn.translate(-0.25, 0.25, 0.25);

                //TODO: use less transforms

                byte[][] colors = GlobeData.get(tile.getLevel()).globePixels;

                if (colors[0].length != 16) {
                    matrixStackIn.popPose();
                    return;
                }

                int lu = combinedLightIn & '\uffff';
                int lv = combinedLightIn >> 16 & '\uffff'; // ok

                matrixStackIn.scale(0.0625f, 0.0625f, 0.0625f);


                renderFace(matrixStackIn, builder, colors, 0, 8, lu, lv, dimension);

                matrixStackIn.mulPose(Const.Y90);

                //up
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Const.XN90);
                matrixStackIn.translate(0, 8, 0);
                renderFaceUp(matrixStackIn, builder, colors, 8, 0, lu, lv, dimension);
                matrixStackIn.popPose();

                //down
                matrixStackIn.pushPose();
                matrixStackIn.translate(0, -8, 0);
                matrixStackIn.mulPose(Const.X90);
                renderFace(matrixStackIn, builder, colors, 16, 0, lu, lv, dimension);
                matrixStackIn.popPose();

                renderFace(matrixStackIn, builder, colors, 8, 8, lu, lv, dimension);
                matrixStackIn.mulPose(Const.Y90);
                renderFace(matrixStackIn, builder, colors, 16, 8, lu, lv, dimension);
                matrixStackIn.mulPose(Const.Y90);
                renderFace(matrixStackIn, builder, colors, 24, 8, lu, lv, dimension);
                break;

            }
        matrixStackIn.popPose();
    }


    public static void renderFace(MatrixStack matrixStackIn, IVertexBuilder builder, byte[][] colors, int ux, int uv, int lu, int lv, String dimension){
        for(int x=0; x<8; x++) {
            matrixStackIn.pushPose();
            for (int y = 0; y < 8; y++) {
                matrixStackIn.translate(0, -1, 0);
                int color = GlobeDataGenerator.getRGB(colors[ux + x][uv + y], dimension);

                float r = (float) ((color >> 16 & 255)) / 255.0F;
                float g = (float) ((color >> 8 & 255)) / 255.0F;
                float b = (float) ((color & 255)) / 255.0F;

                //RendererUtil.addQuadSide();
                // x y z u v r g b a lu lv
                RendererUtil.addQuadSide(builder, matrixStackIn, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, r, g, b, 1, lu, lv, 0, 0, 1);
            }
            matrixStackIn.popPose();
            matrixStackIn.translate(1,0,0);
        }

    }

    public static void renderFaceUp(MatrixStack matrixStackIn, IVertexBuilder builder, byte[][] colors, int ux, int uv, int lu, int lv, String dimension){
        for(int x=0; x<8; x++) {
            matrixStackIn.pushPose();
            for (int y = 0; y < 8; y++) {
                matrixStackIn.translate(0, -1, 0);
                int color = GlobeDataGenerator.getRGB(colors[ux + x][uv + y],dimension);

                float r = ((float) ((color >> 16 & 255)) / 255.0F)*0.775f;
                float g = ((float) ((color >> 8 & 255)) / 255.0F)*0.775f;
                float b = ((float) ((color & 255)) / 255.0F)*0.775f;

                //RendererUtil.addQuadSide();
                // x y z u v r g b a lu lv
                RendererUtil.addQuadSide(builder, matrixStackIn, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, r, g, b, 1, lu, lv, 0, 0, 1);
            }
            matrixStackIn.popPose();
            matrixStackIn.translate(1,0,0);
        }

    }


}