package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.util.IBellConnections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class BellTileMixinRenderer {

    //TODO: re add
    /*
    public static final ModelPart chain = new ModelPart(16, 16, 0, 0);
    public static final ModelPart link = new ModelPart(16, 16, 0, 0);
    public static final ModelPart rope = new ModelPart(16, 16, 0, 0);

        static {
            rope.texOffs(0, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, 0.0F, false);
            rope.setPos(0, 6, 0);
            chain.setPos(0.0F, 0F, 0.0F);
            chain.texOffs(0, 10).addBox(-1.5F, -6.0F, 0.0F, 3.0F, 6.0F, 0.0F, 0.0F, false);
            link.setPos(0.0F, 0.0F, 0.0F);
            chain.addChild(link);
            link.yRot=-1.5708F;
            chain.xRot= (float) Math.PI;
            link.texOffs(6, 10).addBox(-1.5F, -6.0F, 0.0F, 3.0F, 6.0F, 0.0F, 0.0F, false);
        }
*/
    public static LayerDefinition createMesh() {
        return null;
    }

    public static void render(BellBlockEntity tile, float partialTicks, PoseStack matrixStackIn,
                              MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if(tile instanceof IBellConnections){

            IBellConnections.BellConnection connection = ((IBellConnections) tile).getConnected();
            if(connection==null)return;

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0, 0.5);

            if(connection.isRope()) {
                //TODO: fix lighting since none of these methods are shaded properly
                VertexConsumer builder2 = bufferIn.getBuffer(RenderType.entityCutout(Textures.BELL_ROPE_TEXTURE));

                //rope.render(matrixStackIn, builder2, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);
            }
            else if(connection.isChain()){

                //combinedLightIn = WorldRenderer.getCombinedLight(tile.getWorld(), tile.getPos().down());
                int lu = combinedLightIn & '\uffff';
                int lv = combinedLightIn >> 16 & '\uffff'; // ok

                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Textures.CHAIN_TEXTURE);
                VertexConsumer builder = bufferIn.getBuffer(RenderType.cutout());

                float sMinU = sprite.getU0();
                float sMinV = sprite.getV0();
                float sMaxU = sprite.getU1();
                float sMaxV = sprite.getV1();

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

                matrixStackIn.mulPose(RotHlpr.Y45);

                RendererUtil.addQuadSide(builder, matrixStackIn, -w, -0, 0, w, h, 0, minu1, minv, maxu1, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
                RendererUtil.addQuadSide(builder, matrixStackIn, w, -0, 0, -w, h, 0, minu1, minv, maxu1, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
                matrixStackIn.mulPose(RotHlpr.YN90);
                RendererUtil.addQuadSide(builder, matrixStackIn, -w, -0, 0, w, h, 0, minu2, minv, maxu2, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
                RendererUtil.addQuadSide(builder, matrixStackIn, w, -0, 0, -w, h, 0, minu2, minv, maxu2, maxv, col, col, col, 1, lu, lv, 0, 0, 1);
            }
            matrixStackIn.popPose();
        }
    }


}
