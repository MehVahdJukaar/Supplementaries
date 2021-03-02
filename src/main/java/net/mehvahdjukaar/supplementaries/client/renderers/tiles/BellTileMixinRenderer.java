package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.util.IBellConnection;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.BellTileEntity;

public class BellTileMixinRenderer {

    public static final ModelRenderer chain = new ModelRenderer(16, 16, 0, 0);
    public static final ModelRenderer link = new ModelRenderer(16, 16, 0, 0);
    public static final ModelRenderer rope = new ModelRenderer(16, 16, 0, 0);

        static {
            rope.setTextureOffset(0, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, 0.0F, false);
            rope.setRotationPoint(0, 6, 0);
            chain.setRotationPoint(0.0F, 0F, 0.0F);
            chain.setTextureOffset(0, 10).addBox(-1.5F, -6.0F, 0.0F, 3.0F, 6.0F, 0.0F, 0.0F, false);
            link.setRotationPoint(0.0F, 0.0F, 0.0F);
            chain.addChild(link);
            link.rotateAngleY=-1.5708F;
            chain.rotateAngleX= (float) Math.PI;
            link.setTextureOffset(6, 10).addBox(-1.5F, -6.0F, 0.0F, 3.0F, 6.0F, 0.0F, 0.0F, false);
        }


    public static void render(BellTileEntity tile, float partialTicks, MatrixStack matrixStackIn,
                              IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if(tile instanceof IBellConnection ){

            IBellConnection.BellConnection connection = ((IBellConnection) tile).getConnected();
            if(connection==null)return;

            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0, 0.5);

            if(connection.isRope()) {
                //TODO: fix lighting since none of these methods are shaded properly
                IVertexBuilder builder2 = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.BELL_ROPE_TEXTURE));

                rope.render(matrixStackIn, builder2, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);
            }
            else if(connection.isChain()){

                //combinedLightIn = WorldRenderer.getCombinedLight(tile.getWorld(), tile.getPos().down());
                int lu = combinedLightIn & '\uffff';
                int lv = combinedLightIn >> 16 & '\uffff'; // ok

                TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(Textures.CHAIN_TEXTURE);
                IVertexBuilder builder = bufferIn.getBuffer(RenderType.getCutout());

                float sMinU = sprite.getMinU();
                float sMinV = sprite.getMinV();
                float sMaxU = sprite.getMaxU();
                float sMaxV = sprite.getMaxV();

                float atlasscaleU = sMaxU - sMinU;
                float atlasscaleV = sMaxV - sMinV;
                float minu1 = sMinU;
                float minu2 = sMinU + (3f / 16f) * atlasscaleU;
                float minv = sMinV + (11f / 16f) * atlasscaleV;
                float maxu1 = minu2;
                float maxu2 = minu2 + (3f / 16f) * atlasscaleU;
                float maxv = sMaxV;

                float w = 1.5f / 16f;
                float h = 5f / 16f;
                //Minecraft.getInstance().gameSettings.ambientOcclusionStatus.
                float col = 1f;

                matrixStackIn.rotate(Const.Y45);

                RendererUtil.addQuadSide(builder, matrixStackIn, -w, -0, 0, w, h, 0, minu1, minv, maxu1, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
                RendererUtil.addQuadSide(builder, matrixStackIn, w, -0, 0, -w, h, 0, minu1, minv, maxu1, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
                matrixStackIn.rotate(Const.YN90);
                RendererUtil.addQuadSide(builder, matrixStackIn, -w, -0, 0, w, h, 0, minu2, minv, maxu2, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
                RendererUtil.addQuadSide(builder, matrixStackIn, w, -0, 0, -w, h, 0, minu2, minv, maxu2, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
            }
            matrixStackIn.pop();
        }
    }

}
