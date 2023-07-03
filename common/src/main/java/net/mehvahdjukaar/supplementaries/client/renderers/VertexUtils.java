package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

//TODO: move to lib
public class VertexUtils {
    //centered on x,z. aligned on y=0

    public static void addCube(VertexConsumer builder, PoseStack matrixStackIn, float w, float h, int light, int color) {
        addCube(builder, matrixStackIn, 0, 0, w, h, light, color, true, true);
    }

    public static void addCube(VertexConsumer builder, PoseStack matrixStackIn, float uOff, float vOff, float w, float h, int combinedLightIn,
                               int color, boolean up, boolean down) {
        addCube(builder, matrixStackIn, uOff, vOff, w, h, combinedLightIn, color, 1, up, down, false);
    }

    public static void addCube(VertexConsumer builder, PoseStack poseStack,
                               float minU, float minV, float w, float h,
                               int combinedLightIn,
                               int color,
                               float alpha,
                               boolean up, boolean down, boolean wrap) {
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        float maxU = minU + w;
        float maxV = minV + h;
        float maxV2 = minV + w;

        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);
        int a = (int) (255 * alpha);

        float hw = w / 2f;
        float hh = h / 2f;

        float inc = 0;
        poseStack.translate(0, hh, 0);

        poseStack.scale(1, -1, -1);
        for (var d : Direction.values()) {
            float v1 = maxV;
            float t = hw;
            float y0 = -hh;
            float y1 = hh;
            float i = inc;
            if (d.getAxis() == Direction.Axis.Y) {
                if ((!up && d == Direction.UP) || !down) continue;
                t = hh;
                y0 = -hw;
                y1 = hw;
                v1 = maxV2;
            } else if (wrap) {
                inc += w;
            }
            poseStack.pushPose();
            poseStack.mulPose(RotHlpr.rot(d));
            poseStack.translate(0, 0, t);
            addQuad(builder, poseStack, -hw, y0, hw, y1, minU + i, minV, maxU + i, v1, r, g, b, a, lu, lv);
            poseStack.popPose();
        }
    }

    public static int setColorForAge(float age, float phase) {
        float a = (age + phase) % 1;
        float[] col = ColorHelper.getBubbleColor(a);
        return FastColor.ARGB32.color(255, (int) (col[0] * 255), (int) (col[1] * 255), (int) (col[2] * 255));
    }

    public static void renderBubble(VertexConsumer builder, PoseStack poseStack, float w,
                                    TextureAtlasSprite sprite, int combinedLightIn,
                                    BlockPos pos, Level level, float partialTicks) {
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        float atlasScaleU = sprite.getU1() - sprite.getU0();
        float atlasScaleV = sprite.getV1() - sprite.getV0();
        float minU = sprite.getU0();
        float minV = sprite.getV0();
        float maxU = minU + atlasScaleU * w;
        float maxV = minV + atlasScaleV * w;
        float maxV2 = minV + atlasScaleV * w;

        long t = level == null ? System.currentTimeMillis() / 50 : level.getGameTime();
        float time = (Math.floorMod((pos.getX() * 7L + pos.getY() * 9L + pos.getZ() * 13L) + t, 100L) + partialTicks) / 100.0F;

        // w = (1-Mth.sin((float) (time*Math.PI*2)));

        int cUnw = setColorForAge(time, 0);
        int cUne = setColorForAge(time, 0.15f);
        int cUse = setColorForAge(time, 0.55f);
        int cUsw = setColorForAge(time, 0.35f);


        int cDnw = setColorForAge(time, 0.45f);
        int cDne = setColorForAge(time, 0.85f);
        int cDse = setColorForAge(time, 1);
        int cDsw = setColorForAge(time, 0.65f);


        float amp = (float) (ClientConfigs.Blocks.BUBBLE_BLOCK_WOBBLE.get() / 10f);
        w = w - 2 * amp;
        //long time = System.currentTimeMillis();
        float unw = amp * Mth.cos(((float) Math.PI * 2F) * (time + 0));
        float une = amp * Mth.cos(((float) Math.PI * 2F) * (time + 0.25f));
        float use = amp * Mth.cos(((float) Math.PI * 2F) * (time + 0.5f));
        float usw = amp * Mth.cos(((float) Math.PI * 2F) * (time + 0.75f));

        float dnw = use;
        float dne = usw;
        float dse = unw;
        float dsw = une;

        float l = w / 2f;

        //addQuadTop(builder, poseStack, -l+dx1, w, l, l, w, -l, minU, minV, maxU, maxV2, r, g, b, a, lu, lv, 0, 1, 0);
        //top
        vert(builder, poseStack, -l - usw, l + usw, l + usw, minU, maxV2, cUsw, lu, lv, 0, 1, 0);
        vert(builder, poseStack, l + use, l + use, l + use, maxU, maxV2, cUse, lu, lv, 0, 1, 0);
        vert(builder, poseStack, l + une, l + une, -l - une, maxU, minV, cUne, lu, lv, 0, 1, 0);
        vert(builder, poseStack, -l - unw, l + unw, -l - unw, minU, minV, cUnw, lu, lv, 0, 1, 0);


        //addQuadTop(builder, poseStack, -l, 0, -l, l, 0, l, minU, minV, maxU, maxV2, r5, g5, b5, a, lu, lv, 0, -1, 0);

        vert(builder, poseStack, -l - dnw, -l - dnw, -l - dnw, minU, maxV2, cDnw, lu, lv, 0, -1, 0);
        vert(builder, poseStack, l + dne, -l - dne, -l - dne, maxU, maxV2, cDne, lu, lv, 0, -1, 0);
        vert(builder, poseStack, l + dse, -l - dse, l + dse, maxU, minV, cDse, lu, lv, 0, -1, 0);
        vert(builder, poseStack, -l - dsw, -l - dsw, l + dsw, minU, minV, cDsw, lu, lv, 0, -1, 0);

        // north z-
        // x y z u v r g b a lu lv
        //addQuadSide(builder, poseStack, l, 0, -l, -l, w, -l, minU, minV, maxU, maxV, r8, g8, b8, a, lu, lv, 0, 0, 1);
        vert(builder, poseStack, l + dne, -l - dne, -l - dne, minU, maxV, cDne, lu, lv, 0, 0, 1);
        vert(builder, poseStack, -l - dnw, -l - dnw, -l - dnw, maxU, maxV, cDnw, lu, lv, 0, 0, 1);
        vert(builder, poseStack, -l - unw, l + unw, -l - unw, maxU, minV, cUnw, lu, lv, 0, 0, 1);
        vert(builder, poseStack, l + une, l + une, -l - une, minU, minV, cUne, lu, lv, 0, 0, 1);
        // west
        //addQuadSide(builder, poseStack, -l, 0, -l, -l, w, l, minU, minV, maxU, maxV, r6, g6, b6, a, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - dnw, -l - dnw, -l - dnw, minU, maxV, cDnw, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - dsw, -l - dsw, l + dsw, maxU, maxV, cDsw, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - usw, l + usw, l + usw, maxU, minV, cUsw, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - unw, l + unw, -l - unw, minU, minV, cUnw, lu, lv, -1, 0, 0);
        // south
        //addQuadSide(builder, poseStack, -l, 0, l, l, w, l, minU, minV, maxU, maxV, r8, g8, b8, a, lu, lv, 0, 0, -1);
        vert(builder, poseStack, -l - dsw, -l - dsw, l + dsw, minU, maxV, cDsw, lu, lv, 0, 0, -1);
        vert(builder, poseStack, l + dse, -l - dse, l + dse, maxU, maxV, cDse, lu, lv, 0, 0, -1);
        vert(builder, poseStack, l + use, l + use, l + use, maxU, minV, cUse, lu, lv, 0, 0, -1);
        vert(builder, poseStack, -l - usw, l + usw, l + usw, minU, minV, cUsw, lu, lv, 0, 0, -1);
        // east
        //addQuadSide(builder, poseStack, l, 0, l, l, w, -l, minU, minV, maxU, maxV, r6, g6, b6, a, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + dse, -l - dse, l + dse, minU, maxV, cDse, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + dne, -l - dne, -l - dne, maxU, maxV, cDne, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + une, l + une, -l - une, maxU, minV, cUne, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + use, l + use, l + use, minU, minV, cUse, lu, lv, 1, 0, 0);
    }


    //fast 2d quad. Use matrix to put where you want
    public static void addQuad(VertexConsumer builder, PoseStack poseStack,
                               float x0, float y0,
                               float x1, float y1,
                               float u0, float v0,
                               float u1, float v1,
                               int r, int g, int b, int a,
                               int lu, int lv) {
        PoseStack.Pose last = poseStack.last();
        Vector3f vector3f = last.normal().transform(new Vector3f(0, 0, 1));
        float nx = vector3f.x;
        float ny = vector3f.y;
        float nz = vector3f.z;
        //avoids having to multiply 3 times
        vertF(builder, poseStack, x0, y0, 0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
        vertF(builder, poseStack, x1, y0, 0, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        vertF(builder, poseStack, x1, y1, 0, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        vertF(builder, poseStack, x0, y1, 0, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
    }

    public static void addQuadSide(VertexConsumer builder, PoseStack poseStack,
                                   float x0, float y0, float z0,
                                   float x1, float y1, float z1,
                                   float u0, float v0,
                                   float u1, float v1,
                                   float r, float g, float b, float a,
                                   int lu, int lv,
                                   float nx, float ny, float nz) {
        vert(builder, poseStack, x0, y0, z0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
        vert(builder, poseStack, x1, y0, z1, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        vert(builder, poseStack, x1, y1, z1, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        vert(builder, poseStack, x0, y1, z0, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
    }

    public static void addQuadTop(VertexConsumer builder, PoseStack poseStack, float x0, float y0, float z0,
                                  float x1, float y1, float z1,
                                  float u0, float v0,
                                  float u1, float v1,
                                  float r, float g, float b, float a,
                                  int lu, int lv,
                                  float nx, float ny, float nz) {
        vert(builder, poseStack, x0, y0, z0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
        vert(builder, poseStack, x1, y0, z0, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        vert(builder, poseStack, x1, y1, z1, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        vert(builder, poseStack, x0, y1, z1, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
    }


    public static void vert(VertexConsumer builder, PoseStack poseStack, float x, float y, float z,
                            float u, float v,
                            float r, float g, float b, float a,
                            int lu, int lv, float nx, float ny, float nz) {
        //not chained because of MC263524
        builder.vertex(poseStack.last().pose(), x, y, z);
        builder.color(r, g, b, a);
        builder.uv(u, v);
        builder.overlayCoords(0, 10);
        builder.uv2(lu, lv);
        builder.normal(poseStack.last().normal(), nx, ny, nz);
        builder.endVertex();
    }

    public static void vertF(VertexConsumer builder, PoseStack poseStack, float x, float y, float z,
                             float u, float v,
                             int r, int g, int b, int a,
                             int lu, int lv, float nx, float ny, float nz) {
        //not chained because of MC263524
        builder.vertex(poseStack.last().pose(), x, y, z);
        builder.color(r, g, b, a);
        builder.uv(u, v);
        builder.overlayCoords(0, 10);
        builder.uv2(lu, lv);
        builder.normal(nx, ny, nz);
        builder.endVertex();
    }


    public static void vert(VertexConsumer builder, PoseStack poseStack,
                            float x, float y, float z,
                            float u, float v,
                            int color,
                            int lu, int lv, float nx, float ny, float nz) {
        //not chained because of MC263524
        builder.vertex(poseStack.last().pose(), x, y, z);
        builder.color(color);
        builder.uv(u, v);
        builder.overlayCoords(0, 10);
        builder.uv2(lu, lv);
        builder.normal(nx, ny, nz);
        builder.endVertex();
    }


    //RendererUtil.renderFish(builder, matrixStackIn, wo, ho, fishType,240 , combinedOverlayIn);

    public static void renderFish(VertexConsumer builder, PoseStack poseStack, float wo, float ho, int fishType, int combinedLightIn) {
        int textW = 64;
        int textH = 32;
        int fishW = 5;
        int fishH = 4;
        fishType -= 1; //wah
        int fishv = fishType % (textH / fishH);
        int fishu = fishType / (textH / fishH);

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ModTextures.FISHIES_TEXTURE);
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
                vert(builder, poseStack, hw - Math.abs(wo / 2), -hh + ho, +wo, minu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                vert(builder, poseStack, -hw + Math.abs(wo / 2), -hh + ho, -wo, maxu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                vert(builder, poseStack, -hw + Math.abs(wo / 2), hh + ho, -wo, maxu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                vert(builder, poseStack, hw - Math.abs(wo / 2), hh + ho, +wo, minu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                poseStack.mulPose(RotHlpr.Y180);
                float temp = minu;
                minu = maxu;
                maxu = temp;
            }
            lu = 240;
            minu += (atlasscaleU / 2);
            maxu += (atlasscaleU / 2);

        }
    }

}
