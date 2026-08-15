package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.SignText;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextUtils {

    private static final ResourceLocation BEE_MOVIE_SCRIPT = Supplementaries.res("texts/bee_movie.txt");

    private static final List<FormattedCharSequence> CREDITS = new ArrayList<>();
    private static final List<FormattedCharSequence> BEE_MOVIE = new ArrayList<>();

    private static final float SCALING_FACTOR = 16 * 8F;
    private static final float SCALE = 1 / SCALING_FACTOR;
    private static final int LINE_WIDTH = Mth.floor((1 - (2 * 0.125f)) * SCALING_FACTOR);

    private static int creditsGeneration = -1;

    public static void clearCachedTexts() {
        BEE_MOVIE.clear();
        CREDITS.clear();
        creditsGeneration = -1;
    }

    public static void renderBeeMovie(PoseStack matrixStack, MultiBufferSource bufferIn, int light,
                                      Font fontRenderer, float side) {
        if (BEE_MOVIE.isEmpty()) {
            BEE_MOVIE.addAll(splitToLines(readTextFile(BEE_MOVIE_SCRIPT)));
        }
        renderScrollingText(matrixStack, bufferIn, light, fontRenderer, side, BEE_MOVIE);
    }

    public static void renderCredits(PoseStack matrixStack, MultiBufferSource bufferIn, int light,
                                     Font fontRenderer, float side) {
        // credits are fetched in the background, so they can show up long after the first time this runs
        if (creditsGeneration != Credits.generation()) {
            creditsGeneration = Credits.generation();
            CREDITS.clear();
            CREDITS.addAll(splitToLines(Credits.INSTANCE.createCreditsText()));
        }
        renderScrollingText(matrixStack, bufferIn, light, fontRenderer, side, CREDITS);
    }

    private static List<FormattedCharSequence> splitToLines(String text) {
        return Minecraft.getInstance().font.split(TextUtil.parseText(text, null), LINE_WIDTH);
    }

    private static String readTextFile(ResourceLocation path) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(path);
        if (resource.isPresent()) {
            try (BufferedReader reader = resource.get().openAsReader()) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException ignored) {
            }
        }
        Supplementaries.LOGGER.error("Failed to read text file {}", path);
        return "";
    }

    private static void renderScrollingText(PoseStack matrixStack, MultiBufferSource bufferIn, int light,
                                            Font fontRenderer, float side, List<FormattedCharSequence> text) {
        if (text.isEmpty()) return;

        long time = System.currentTimeMillis();

        int numberOfLines = text.size();

        long timePerLine = 800L;

        float progress = ((time % (numberOfLines * timePerLine)) / (float) timePerLine);

        matrixStack.scale(SCALE, -SCALE, SCALE);

        float lin = progress;
        float offset = 8 * (progress % 1);
        int startLin = (int) lin;
        float o = lin - startLin;

        float borderY = 0.125f;

        matrixStack.pushPose();
        for (int n = 0; n < 13; ++n) {
            if (startLin + n >= numberOfLines) break;
            FormattedCharSequence str = text.get(startLin + n);
            float dx = (-fontRenderer.width(str) / 2f) + 0.5f;
            int a = (int) (255 * (-Math.pow(((-o + n - 6f) / 7.2f), 2) + 1));
            int col = (int) (255 * side);
            int rgba = FastColor.ARGB32.color(a, col, col, col);

            fontRenderer.drawInBatch(str, dx, borderY * SCALING_FACTOR - (offset) + 8 * n, rgba, false,
                    matrixStack.last().pose(), bufferIn, Font.DisplayMode.NORMAL, 0, light);
        }
        matrixStack.popPose();
    }

    public static void renderTextHolderLines(TextHolder textHolder, int ySeparation, Font font, PoseStack poseStack, MultiBufferSource buffer,
                                             TextUtil.RenderProperties properties) {
        for (int i = 0; i < textHolder.size(); i++) {
            TextUtil.renderLine(textHolder.getRenderMessages(i, font), font, ySeparation * i, poseStack, buffer, properties);
        }


    }

    public static void renderSignText(SignText signText, Font font, PoseStack poseStack,
                                      MultiBufferSource buffer,
                                      int light, Vector3f normal, LOD lod, boolean filtered,
                                      int lineHeight, int lineWidth,
                                      float colorMult) {
        TextUtil.RenderProperties properties = TextUtil.renderProperties(signText.getColor(),
                signText.hasGlowingText(), colorMult, light, Style.EMPTY, normal, lod::isVeryNear);

        FormattedCharSequence[] formattedCharSequences = signText.getRenderMessages(filtered, (component) -> {
            List<FormattedCharSequence> list = font.split(component, lineWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.getFirst();
        });
        for (int i = 0; i < formattedCharSequences.length; i++) {
            TextUtil.renderLine(formattedCharSequences[i], font, lineHeight * i, poseStack, buffer, properties);
        }

    }


}
