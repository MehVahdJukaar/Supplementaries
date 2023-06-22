package net.mehvahdjukaar.supplementaries.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.BannerPatternTooltip;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;

public class BannerPatternTooltipComponent implements ClientTooltipComponent {

    private final int size = ClientConfigs.Tweaks.TOOLTIP_IMAGE_SIZE.get();
    private final BannerPatternTooltip tooltip;

    public BannerPatternTooltipComponent(BannerPatternTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return size + 2;
    }

    @Override
    public int getWidth(Font pFont) {
        return size;
    }

    @Override
    public void renderImage(Font pFont, int x, int y, GuiGraphics graphics) {
        graphics.pose().pushPose();

        var mat = BuiltInRegistries.BANNER_PATTERN.getTag(tooltip.pattern())
                .flatMap(n -> n.stream().findAny()).flatMap(Holder::unwrapKey).map(Sheets::getBannerMaterial);
        if (mat.isPresent()) {
            var sprite = mat.get().sprite();
            RenderSystem.enableBlend();
            var contents = sprite.contents();
            int width = contents.width();
            int height = contents.height();
            RenderUtil.blitSpriteSection(graphics, x, y, size, size, (16f) / width, (16f / height) * 12, (int) (20f / 64 * width), (int) (20f / 64 * height), sprite);
        }

        graphics.pose().popPose();
    }
}