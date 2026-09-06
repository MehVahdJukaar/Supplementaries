package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

public class FlagLoomRenderer implements LoomItemRenderer {

    public static final FlagLoomRenderer INSTANCE = new FlagLoomRenderer();

    private static final int FACE_WIDTH = 24;
    private static final int FACE_HEIGHT = 16;

    private static final int ICON_X = 1;
    private static final int ICON_Y = 3;
    private static final int ICON_WIDTH = 12;
    private static final int ICON_HEIGHT = 8;

    @Override
    public boolean render(GuiGraphics graphics, ItemStack bannerSlotStack, ItemStack result,
                          @Nullable BannerPatternLayers patterns, int leftPos, int topPos, float partialTicks) {
        if (patterns == null || !(bannerSlotStack.getItem() instanceof FlagItem flag)) return true;

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(leftPos + 139d, topPos + 52d, 0.0D);
        pose.scale(24.0F, -24.0F, 1.0F);
        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(RotHlpr.Y90);
        pose.mulPose(RotHlpr.X90);
        pose.scale(1.125F, 1.125F, 1.125F);
        pose.translate(-1, -0.5, -1.1875);
        Lighting.setupForFlatItems();

        FlagBlockTileRenderer.renderPatterns(pose, buffer, patterns, 15728880, flag.getColor());

        pose.popPose();
        buffer.endBatch();
        Lighting.setupFor3DItems();
        return true;
    }

    @Override
    public boolean renderPatternIcon(GuiGraphics graphics, ItemStack bannerSlotStack,
                                     Holder<BannerPattern> pattern, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        float cx = x + ICON_X + ICON_WIDTH / 2f;
        float cy = y + ICON_Y + ICON_HEIGHT / 2f;
        pose.translate(cx, cy, 0);
        pose.mulPose(RotHlpr.Z90);
        pose.translate(-cx, -cy, 0);
        blitFace(graphics, ModMaterials.FLAG_BASE_MATERIAL, DyeColor.GRAY, x, y);
        blitFace(graphics, ModMaterials.FLAG_MATERIALS.apply(pattern.value()), DyeColor.WHITE, x, y);
        pose.popPose();
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        return true;
    }

    private static void blitFace(GuiGraphics graphics, Material material, DyeColor color, int x, int y) {
        int tint = color.getTextureDiffuseColor();
        graphics.setColor(FastColor.ARGB32.red(tint) / 255f, FastColor.ARGB32.green(tint) / 255f,
                FastColor.ARGB32.blue(tint) / 255f, 1);
        RenderUtil.blitSpriteSection(graphics, x + ICON_X, y + ICON_Y, ICON_WIDTH, ICON_HEIGHT,
                0, 0, FACE_WIDTH, FACE_HEIGHT, material.sprite());
    }
}
