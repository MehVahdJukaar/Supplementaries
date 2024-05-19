package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

//TODO: move to lib
public class VertexUtils {
    //centered on x,z. aligned on y=0

    public static int setColorForAge(float age, float phase) {
        float a = (age + phase) % 1;
        float[] col = ColorHelper.getBubbleColor(a);
        return FastColor.ARGB32.color(255, (int) (col[0] * 255), (int) (col[1] * 255), (int) (col[2] * 255));
    }

    public static void renderBubble(VertexConsumer builder, PoseStack poseStack,
                                    int combinedLightIn, BlockPos pos, Level level, float partialTicks) {
        TextureAtlasSprite sprite = ModMaterials.BUBBLE_BLOCK_MATERIAL.sprite();
        builder = sprite.wrap(builder);

        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        float minU = 0;
        float minV = 0;
        float maxU = 1;
        float maxV = 1;

        float w = 1;

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
        vert(builder, poseStack, -l - usw, l + usw, l + usw, minU, maxV, cUsw, lu, lv, 0, 1, 0);
        vert(builder, poseStack, l + use, l + use, l + use, maxU, maxV, cUse, lu, lv, 0, 1, 0);
        vert(builder, poseStack, l + une, l + une, -l - une, maxU, minV, cUne, lu, lv, 0, 1, 0);
        vert(builder, poseStack, -l - unw, l + unw, -l - unw, minU, minV, cUnw, lu, lv, 0, 1, 0);


        //addQuadTop(builder, poseStack, -l, 0, -l, l, 0, l, minU, minV, maxU, maxV2, r5, g5, b5, a, lu, lv, 0, -1, 0);
        //bottom
        vert(builder, poseStack, -l - dnw, -l - dnw, -l - dnw, minU, maxV, cDnw, lu, lv, 0, -1, 0);
        vert(builder, poseStack, l + dne, -l - dne, -l - dne, maxU, maxV, cDne, lu, lv, 0, -1, 0);
        vert(builder, poseStack, l + dse, -l - dse, l + dse, maxU, minV, cDse, lu, lv, 0, -1, 0);
        vert(builder, poseStack, -l - dsw, -l - dsw, l + dsw, minU, minV, cDsw, lu, lv, 0, -1, 0);

        // north z-
        // x y z u v r g b a lu lv
        //addQuadSide(builder, poseStack, l, 0, -l, -l, w, -l, minU, minV, maxU, maxV, r8, g8, b8, a, lu, lv, 0, 0, 1);
        vert(builder, poseStack, l + dne, -l - dne, -l - dne, minU, maxV, cDne, lu, lv, 0, 0, -1);
        vert(builder, poseStack, -l - dnw, -l - dnw, -l - dnw, maxU, maxV, cDnw, lu, lv, 0, 0, -1);
        vert(builder, poseStack, -l - unw, l + unw, -l - unw, maxU, minV, cUnw, lu, lv, 0, 0, -1);
        vert(builder, poseStack, l + une, l + une, -l - une, minU, minV, cUne, lu, lv, 0, 0, -1);
        // west
        //addQuadSide(builder, poseStack, -l, 0, -l, -l, w, l, minU, minV, maxU, maxV, r6, g6, b6, a, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - dnw, -l - dnw, -l - dnw, minU, maxV, cDnw, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - dsw, -l - dsw, l + dsw, maxU, maxV, cDsw, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - usw, l + usw, l + usw, maxU, minV, cUsw, lu, lv, -1, 0, 0);
        vert(builder, poseStack, -l - unw, l + unw, -l - unw, minU, minV, cUnw, lu, lv, -1, 0, 0);
        // south
        //addQuadSide(builder, poseStack, -l, 0, l, l, w, l, minU, minV, maxU, maxV, r8, g8, b8, a, lu, lv, 0, 0, -1);
        vert(builder, poseStack, -l - dsw, -l - dsw, l + dsw, minU, maxV, cDsw, lu, lv, 0, 0, 1);
        vert(builder, poseStack, l + dse, -l - dse, l + dse, maxU, maxV, cDse, lu, lv, 0, 0, 1);
        vert(builder, poseStack, l + use, l + use, l + use, maxU, minV, cUse, lu, lv, 0, 0, 1);
        vert(builder, poseStack, -l - usw, l + usw, l + usw, minU, minV, cUsw, lu, lv, 0, 0, 1);
        // east
        //addQuadSide(builder, poseStack, l, 0, l, l, w, -l, minU, minV, maxU, maxV, r6, g6, b6, a, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + dse, -l - dse, l + dse, minU, maxV, cDse, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + dne, -l - dne, -l - dne, maxU, maxV, cDne, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + une, l + une, -l - une, maxU, minV, cUne, lu, lv, 1, 0, 0);
        vert(builder, poseStack, l + use, l + use, l + use, minU, minV, cUse, lu, lv, 1, 0, 0);
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
        builder.normal(poseStack.last().normal(), nx, ny, nz);
        builder.endVertex();
    }


    //RendererUtil.renderFish(builder, matrixStackIn, wo, ho, fishType,240 , combinedOverlayIn);

    public static void renderFish(MultiBufferSource buffers, PoseStack poseStack, float wo, float ho, int fishType, int combinedLightIn) {
        int textW = 64;
        int textH = 32;
        int fishW = 5;
        int fishH = 4;
        fishType -= 1; //wah
        int fishv = fishType % (textH / fishH);
        int fishu = fishType / (textH / fishH);

        VertexConsumer builder = ModMaterials.FISHIES.buffer(buffers, RenderType::entityCutout);

        float w = fishW / (float) textW;
        float h = fishH / (float) textH;
        float hw = 4 * w / 2f;
        float hh = 2 * h / 2f;
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        float minu = 0 * fishu * w;
        float minv = 0 * fishv * h;
        float maxu = 1 * w + minu;
        float maxv = 1 * h + minv;


        for (int k = 0; k < 2; k++) {
            for (int j = 0; j < 2; j++) {
                VertexUtil.vert(builder, poseStack, hw - Math.abs(wo / 2), -hh + ho, +wo, minu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                VertexUtil.vert(builder, poseStack, -hw + Math.abs(wo / 2), -hh + ho, -wo, maxu, maxv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                VertexUtil.vert(builder, poseStack, -hw + Math.abs(wo / 2), hh + ho, -wo, maxu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                VertexUtil.vert(builder, poseStack, hw - Math.abs(wo / 2), hh + ho, +wo, minu, minv, 1, 1, 1, 1, lu, lv, 0, 1, 0);
                poseStack.mulPose(RotHlpr.Y180);
                float temp = minu;
                minu = maxu;
                maxu = temp;
            }
            lu = 240;
            minu += (1 / 2);
            maxu += (1 / 2);

        }
    }


}
