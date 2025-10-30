package net.mehvahdjukaar.supplementaries.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.PaintingTooltip;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class PaintingTooltipComponent implements ClientTooltipComponent {

    private final PaintingVariant pattern;
    private final int height;
    private final int width;

    public PaintingTooltipComponent(PaintingTooltip tooltip) {
        RegistryAccess ra = Minecraft.getInstance().level.registryAccess();
        var painting = tooltip.data().read(ra.createSerializationContext(NbtOps.INSTANCE), Painting.VARIANT_MAP_CODEC);
        this.pattern = painting.getOrThrow().value();
        float h = pattern.height() * 16;
        float w = pattern.width() * 16;
        int size = ClientConfigs.Tweaks.TOOLTIP_IMAGE_SIZE.get();
        if (h > w) {
            this.height = size;
            this.width = (int) ((size / h) * w);
        } else {
            this.width = size;
            this.height = (int) ((size / w) * h);
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
    public void renderImage(Font pFont, int x, int y, GuiGraphics graphics) {
        graphics.pose().pushPose();
        PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
        var sprite = paintingTextureManager.get(pattern);

        RenderSystem.enableBlend();

        graphics.blit(x, y, 0, width, height, sprite);

        graphics.pose().popPose();
    }
}