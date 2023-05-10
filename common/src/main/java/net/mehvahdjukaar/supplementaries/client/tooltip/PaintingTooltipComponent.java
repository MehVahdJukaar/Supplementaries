package net.mehvahdjukaar.supplementaries.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.PaintingTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class PaintingTooltipComponent implements ClientTooltipComponent {

    private static final int MAX_SIZE = 80;

    private final PaintingVariant pattern;
    private final int height;
    private final int width;

    public PaintingTooltipComponent(PaintingTooltip tooltip) {
        this.pattern = tooltip.pattern();
        float h = pattern.getHeight();
        float w = pattern.getWidth();
        if (h > w) {
            this.height = MAX_SIZE;
            this.width = (int)((MAX_SIZE / h) * w);
        } else {
            this.width = MAX_SIZE;
            this.height = (int)((MAX_SIZE / w) * h);
        }
    }

    @Override
    public int getHeight() {
        return height + 2;
    }

    @Override
    public int getWidth(Font pFont) {
        return width;
    }

    @Override
    public void renderImage(Font pFont, int x, int y, PoseStack poseStack, ItemRenderer pItemRenderer) {
        poseStack.pushPose();
        PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
        var sprite = paintingTextureManager.get(pattern);

        RenderSystem.enableBlend();
        var c = sprite.contents();

        RenderUtil.blitSprite(poseStack, x, y, width, height, 0, 0, c.width(), c.height(), sprite);

        poseStack.popPose();
    }
}