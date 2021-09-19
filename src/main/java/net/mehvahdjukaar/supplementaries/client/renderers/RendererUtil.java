package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.flywheel.FlywheelPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.World;

import java.util.Random;

public class RendererUtil {
    //centered on x,z. aligned on y=0

    //stuff that falling sand uses. for some reason renderBlock doesn't use correct light level
    public static void renderBlockModel(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer,
                                        BlockRendererDispatcher blockRenderer, World world, BlockPos pos) {
        try {
            for (RenderType type : RenderType.chunkBufferLayers()) {
                if (RenderTypeLookup.canRenderInLayer(state, type)) {
                    renderBlockModel(state, matrixStack, buffer, blockRenderer, world, pos, type);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void renderBlockModel(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer,
                                        BlockRendererDispatcher blockRenderer, World world, BlockPos pos, RenderType type) {

        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
        blockRenderer.getModelRenderer().tesselateBlock(world,
                blockRenderer.getBlockModel(state), state, pos, matrixStack,
                buffer.getBuffer(type), false, new Random(), 0,
                OverlayTexture.NO_OVERLAY);
        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
    }

    public static void renderBlockModel(ResourceLocation modelLocation, MatrixStack matrixStack, IRenderTypeBuffer buffer,
                                        BlockRendererDispatcher blockRenderer, int light, int overlay, boolean cutout) {

        blockRenderer.getModelRenderer().renderModel(matrixStack.last(),
                buffer.getBuffer(cutout ? Atlases.cutoutBlockSheet() : Atlases.solidBlockSheet()),
                null,
                blockRenderer.getBlockModelShaper().getModelManager().getModel(modelLocation),
                1.0F, 1.0F, 1.0F,
                light, overlay);
    }


    public static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
                               int color, float a, int combinedOverlayIn, boolean up, boolean down, boolean fakeshading, boolean flippedY) {
        addCube(builder, matrixStackIn, 0, 0, w, h, sprite, combinedLightIn, color, a, up, down, fakeshading, flippedY, false);
    }

    public static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float uOff, float vOff, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
                               int color, float a, int combinedOverlayIn, boolean up, boolean down, boolean fakeshading, boolean flippedY) {
        addCube(builder, matrixStackIn, uOff, vOff, w, h, sprite, combinedLightIn, color, a, up, down, fakeshading, flippedY, false);
    }


    public static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float uOff, float vOff, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
                               int color, float a, boolean up, boolean down, boolean fakeshading, boolean flippedY, boolean wrap) {
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff'; // ok
        float atlasScaleU = sprite.getU1() - sprite.getU0();
        float atlasScaleV = sprite.getV1() - sprite.getV0();
        float minU = sprite.getU0() + atlasScaleU * uOff;
        float minV = sprite.getV0() + atlasScaleV * vOff;
        float maxU = minU + atlasScaleU * w;
        float maxV = minV + atlasScaleV * h;
        float maxV2 = minV + atlasScaleV * w;

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
            addQuadTop(builder, matrixStackIn, -hw, h, hw, hw, h, -hw, minU, minV, maxU, maxV2, r, g, b, a, lu, lv, 0, 1, 0);
        // down
        if (down)
            addQuadTop(builder, matrixStackIn, -hw, 0, -hw, hw, 0, hw, minU, minV, maxU, maxV2, r5, g5, b5, a, lu, lv, 0, -1, 0);


        if (flippedY) {
            float temp = minV;
            minV = maxV;
            maxV = temp;
        }
        float inc = 0;
        if (wrap) {
            inc = atlasScaleU * w;
        }

        // north z-
        // x y z u v r g b a lu lv
        addQuadSide(builder, matrixStackIn, hw, 0, -hw, -hw, h, -hw, minU, minV, maxU, maxV, r8, g8, b8, a, lu, lv, 0, 0, 1);
        // west
        addQuadSide(builder, matrixStackIn, -hw, 0, -hw, -hw, h, hw, minU + inc, minV, maxU + inc, maxV, r6, g6, b6, a, lu, lv, -1, 0, 0);
        // south
        addQuadSide(builder, matrixStackIn, -hw, 0, hw, hw, h, hw, minU + 2 * inc, minV, maxU + 2 * inc, maxV, r8, g8, b8, a, lu, lv, 0, 0, -1);
        // east
        addQuadSide(builder, matrixStackIn, hw, 0, hw, hw, h, -hw, minU + 3 * inc, minV, maxU + 3 * inc, maxV, r6, g6, b6, a, lu, lv, 1, 0, 0);
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

    public static void addQuadSide(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
                                   float b, float a, int lu, int lv, float nx, float ny, float nz, TextureAtlasSprite sprite) {

        u0 = getRelativeU(sprite, u0);
        u1 = getRelativeU(sprite, u1);
        v0 = getRelativeV(sprite, v0);
        v1 = getRelativeV(sprite, v1);

        addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y0, z1, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x0, y1, z0, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
    }

    public static void addQuadTop(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
                                  float b, float a, int lu, int lv, float nx, float ny, float nz) {
        addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y0, z0, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        addVert(builder, matrixStackIn, x0, y1, z1, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
    }


    public static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
                               float b, float a, int lu, int lv, float nx, float ny, float nz) {
        builder.vertex(matrixStackIn.last().pose(), x, y, z).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lu, lv)
                .normal(matrixStackIn.last().normal(), nx, ny, nz).endVertex();
    }

    public static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
                               float b, float a, int lu, int lv, float nx, float ny, float nz, TextureAtlasSprite sprite) {
        builder.vertex(matrixStackIn.last().pose(), x, y, z).color(r, g, b, a).uv(getRelativeU(sprite,u), getRelativeV(sprite,v))
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lu, lv).normal(matrixStackIn.last().normal(), nx, ny, nz).endVertex();
    }

    public static float getRelativeU(TextureAtlasSprite sprite, float u){
        float f = sprite.getU1() - sprite.getU0();
        return sprite.getU0() + f * u;
    }

    public static float getRelativeV(TextureAtlasSprite sprite, float v){
        float f = sprite.getV1() - sprite.getV0();
        return sprite.getV0() + f * v;
    }


    //RendererUtil.renderFish(builder, matrixStackIn, wo, ho, fishType,240 , combinedOverlayIn);

    public static void renderFish(IVertexBuilder builder, MatrixStack matrixStackIn, float wo, float ho, int fishType, int combinedLightIn) {
        int textW = 64;
        int textH = 32;
        int fishW = 5;
        int fishH = 4;

        int fishv = fishType % (textH / fishH);
        int fishu = fishType / (textH / fishH);

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.FISHIES_TEXTURE);
        float w = fishW / (float) textW;
        float h = fishH / (float) textH;
        float hw = 4 * w / 2f;
        float hh = 2 * h / 2f;
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        float atlasscaleU = sprite.getU1() - sprite.getU0();
        float atlasscaleV = sprite.getV1() - sprite.getV0();
        float minu = sprite.getU0() + atlasscaleU * fishu * w;
        float minv = sprite.getV0() + atlasscaleV * fishv * h;
        float maxu = atlasscaleU * w + minu;
        float maxv = atlasscaleV * h + minv;


        for (int k = 0; k < 2; k++) {
            for (int j = 0; j < 2; j++) {
                addVert(builder, matrixStackIn, hw - Math.abs(wo / 2), -hh + ho, +wo, minu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                addVert(builder, matrixStackIn, -hw + Math.abs(wo / 2), -hh + ho, -wo, maxu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                addVert(builder, matrixStackIn, -hw + Math.abs(wo / 2), hh + ho, -wo, maxu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                addVert(builder, matrixStackIn, hw - Math.abs(wo / 2), hh + ho, +wo, minu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                matrixStackIn.mulPose(Const.Y180);
                float temp = minu;
                minu = maxu;
                maxu = temp;
            }
            lu = 240;
            minu += (atlasscaleU / 2);
            maxu += (atlasscaleU / 2);

        }
    }

    private static int formatLength = 8;

    public static void changeVertexFormat(int length){
        formatLength = length;
    }

    private static void checkShaders(){
        if(CompatHandler.flywheel){
            changeVertexFormat(FlywheelPlugin.areShadersOn() ? 9 : 8);
        }
    }

    /**
     * moves baked vertices in a direction by amount
     */
    public static int[] moveVertices(int[] v, Direction dir, float amount) {
        checkShaders();
        int axis = dir.getAxis().ordinal();
        float step = amount * dir.getAxisDirection().getStep();
        for (int i = 0; i < v.length / formatLength; i++) {
            float original = Float.intBitsToFloat(v[i * formatLength + axis]);
            v[i * formatLength + axis] = Float.floatToIntBits(original + step);
        }
        return v;
    }

    /**
     * moves baked vertices by amount
     */
    public static int[] moveVertices(int[] v, float x, float y, float z) {
        checkShaders();
        for (int i = 0; i < v.length / formatLength; i++) {
            float originalX = Float.intBitsToFloat(v[i * formatLength]);
            v[i * formatLength] = Float.floatToIntBits(originalX + x);

            float originalY = Float.intBitsToFloat(v[i * formatLength + 1]);
            v[i * formatLength + 1] = Float.floatToIntBits(originalY + y);

            float originalZ = Float.intBitsToFloat(v[i * formatLength + 2]);
            v[i * formatLength + 2] = Float.floatToIntBits(originalZ + z);
        }
        return v;
    }


    /**
     * scale baked vertices by amount
     */
    public static int[] scaleVertices(int[] v, float scale) {
        checkShaders();
        for (int i = 0; i < v.length / formatLength; i++) {
            float originalX = Float.intBitsToFloat(v[i * formatLength]);
            v[i * formatLength] = Float.floatToIntBits(originalX * scale);

            float originalY = Float.intBitsToFloat(v[i * formatLength + 1]);
            v[i * formatLength + 1] = Float.floatToIntBits(originalY * scale);

            float originalZ = Float.intBitsToFloat(v[i * formatLength + 2]);
            v[i * formatLength + 2] = Float.floatToIntBits(originalZ * scale);
        }
        return v;
    }

    public static int[] transformVertices(int[] v, MatrixStack stack) {
        Vector4f vector4f = new Vector4f(0, 0, 0, 1.0F);
        vector4f.transform(stack.last().pose());
        v = moveVertices(v, vector4f.x(), vector4f.y(), vector4f.z());
        return v;
    }


}
