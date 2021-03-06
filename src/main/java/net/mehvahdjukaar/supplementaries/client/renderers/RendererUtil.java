package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class RendererUtil {
    //centered on x,z. aligned on y=0

    //stuff that falling sand uses. for some reason renderBlock doesn't use correct light level
    public static void renderBlockPlus(BlockState state, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                                       BlockRendererDispatcher blockRenderer, World world, BlockPos pos){
        try {
            for (RenderType type : RenderType.getBlockRenderTypes()) {
                if (RenderTypeLookup.canRenderInLayer(state, type)) {
                    renderBlockPlus(state, matrixStackIn, bufferIn, blockRenderer, world, pos, type);
                }
            }
        }catch (Exception ignored){}

    }

    public static void renderBlockPlus(BlockState state, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                                       BlockRendererDispatcher blockRenderer, World world, BlockPos pos, RenderType type){
        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
        blockRenderer.getBlockModelRenderer().renderModel(world,
                blockRenderer.getModelForState(state), state, pos, matrixStackIn,
                bufferIn.getBuffer(type), false, new Random(),0,
                OverlayTexture.NO_OVERLAY);
        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
    }


    public static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
                               int color, float a, int combinedOverlayIn, boolean up, boolean down, boolean fakeshading, boolean flippedY) {
        addCube(builder,matrixStackIn,0,0,w,h,sprite,combinedLightIn,color,a,combinedOverlayIn,up,down,fakeshading,flippedY,false);
    }
    public static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn,float uOff, float vOff, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
                               int color, float a, int combinedOverlayIn, boolean up, boolean down, boolean fakeshading, boolean flippedY) {
        addCube(builder,matrixStackIn,uOff,vOff,w,h,sprite,combinedLightIn,color,a,combinedOverlayIn,up,down,fakeshading,flippedY,false);
    }



    //TODO: cache sprite coordinates?
    //TODO: wrap around faucet water texture
    public static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn,float uOff, float vOff, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
                               int color, float a, int combinedOverlayIn, boolean up, boolean down, boolean fakeshading, boolean flippedY, boolean wrap) {
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff'; // ok
        float atlasscaleU = sprite.getMaxU() - sprite.getMinU();
        float atlasscaleV = sprite.getMaxV() - sprite.getMinV();
        float minu = sprite.getMinU() + atlasscaleU * uOff;
        float minv = sprite.getMinV() + atlasscaleV * vOff;
        float maxu = minu + atlasscaleU * w;
        float maxv = minv + atlasscaleV * h;
        float maxv2 = minv + atlasscaleV * w;

        float r = (float) ((color >> 16 & 255)) / 255.0F;
        float g = (float) ((color >> 8 & 255)) / 255.0F;
        float b = (float) ((color & 255)) / 255.0F;


        // float a = 1f;// ((color >> 24) & 0xFF) / 255f;
        // shading:

        float r8, g8, b8, r6, g6, b6, r5, g5, b5;

        r8 = r6 = r5 = r;
        g8 = g6 = g5 = g;
        b8 = b6 = b5 = b;
        //TODO: make this affect uv values not rgb
        if (fakeshading) {
            float s1 = 0.8f, s2 = 0.6f, s3 = 0.5f;
            // 80%: s,n
            r8 *= s1;
            g8 *= s1;
            b8 *= s1;
            // 60%: e,w
            r6 *= s2;
            g6 *= s2;
            b6 *= s2;
            // 50%: d
            r5 *= s3;
            g5 *= s3;
            b5 *= s3;
            //100%

        }

        float hw = w / 2f;
        float hh = h / 2f;

        // up
        if (up)
            addQuadTop(builder, matrixStackIn, -hw, h, hw, hw, h, -hw, minu, minv, maxu, maxv2, r, g, b, a, lu, lv, 0, 1, 0);
        // down
        if (down)
            addQuadTop(builder, matrixStackIn, -hw, 0, -hw, hw, 0, hw, minu, minv, maxu, maxv2, r5, g5, b5, a, lu, lv, 0, -1, 0);


        if (flippedY) {
            float temp = minv;
            minv = maxv;
            maxv = temp;
        }
        float inc = 0;
        if (wrap){
            inc = atlasscaleU * w;
        }

        // north z-
        // x y z u v r g b a lu lv
        addQuadSide(builder, matrixStackIn, hw, 0, -hw, -hw, h, -hw, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv, 0, 0, 1);
        // west
        addQuadSide(builder, matrixStackIn, -hw, 0, -hw, -hw, h, hw, minu+inc, minv, maxu+inc, maxv, r6, g6, b6, a, lu, lv, -1, 0, 0);
        // south
        addQuadSide(builder, matrixStackIn, -hw, 0, hw, hw, h, hw, minu+2*inc, minv, maxu+2*inc, maxv, r8, g8, b8, a, lu, lv, 0, 0, -1);
        // east
        addQuadSide(builder, matrixStackIn, hw, 0, hw, hw, h, -hw, minu+3*inc, minv, maxu+3*inc, maxv, r6, g6, b6, a, lu, lv, 1, 0, 0);
    }

    /*

        public static void addDoubleQuadSide(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
                                             float b, float a, int lu, int lv){
            addQuadSide(builder, matrixStackIn, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, lu, lv);
            addQuadSide(builder, matrixStackIn, x1, y0, z1, x0, y1, z0, u0, v0, u1, v1, r, g, b, a, lu, lv);
        }
    */

    public static void addQuadSide(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
                                   float b, float a, int lu, int lv, float nx, float ny, float nz) {
        addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y0, z1, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x0, y1, z0, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
    }

    /*

        public static void addQuadSideF(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
                                        float b, float a, int lu, int lv) {
            addVert(builder, matrixStackIn, x0, y0, z0, u0, v0, r, g, b, a, lu, lv);
            addVert(builder, matrixStackIn, x1, y0, z1, u1, v0, r, g, b, a, lu, lv);
            addVert(builder, matrixStackIn, x1, y1, z1, u1, v1, r, g, b, a, lu, lv);
            addVert(builder, matrixStackIn, x0, y1, z0, u0, v1, r, g, b, a, lu, lv);
        }
    */

    public static void addQuadTop(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
                                  float b, float a, int lu, int lv, float nx, float ny, float nz) {
        addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y0, z0, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x0, y1, z1, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
    }


    public static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
                               float b, float a, int lu, int lv, float nx, float ny, float nz) {
        builder.pos(matrixStackIn.getLast().getMatrix(), x, y, z).color(r, g, b, a).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(lu, lv)
                .normal(matrixStackIn.getLast().getNormal(), nx, ny, nz).endVertex();
    }


    public static void renderFish(IVertexBuilder builder, MatrixStack matrixStackIn, float wo, float ho, int fishType, int combinedLightIn,
                                  int combinedOverlayIn) {
        int fishv = fishType%4;
        int fishu = fishType/4;

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(Textures.FISHIES_TEXTURE);
        float w = 5 / 16f;
        float h = 4 / 16f;
        float hw = w / 2f;
        float hh = h / 2f;
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        float atlasscaleU = sprite.getMaxU() - sprite.getMinU();
        float atlasscaleV = sprite.getMaxV() - sprite.getMinV();
        float minu = sprite.getMinU() + atlasscaleU * fishu * h;
        float minv = sprite.getMinV() + atlasscaleV * fishv * h;
        float maxu = atlasscaleU * w + minu;
        float maxv = atlasscaleV * h + minv;
        for (int j = 0; j < 2; j++) {
            addVert(builder, matrixStackIn, hw - Math.abs(wo / 2), -hh + ho, +wo, minu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
            addVert(builder, matrixStackIn, -hw + Math.abs(wo / 2), -hh + ho, -wo, maxu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
            addVert(builder, matrixStackIn, -hw + Math.abs(wo / 2), hh + ho, -wo, maxu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
            addVert(builder, matrixStackIn, hw - Math.abs(wo / 2), hh + ho, +wo, minu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
            matrixStackIn.rotate(Const.Y180);
            float temp = minu;
            minu = maxu;
            maxu = temp;
        }
    }
}
