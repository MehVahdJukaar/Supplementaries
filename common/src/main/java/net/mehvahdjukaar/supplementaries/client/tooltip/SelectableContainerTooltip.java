package net.mehvahdjukaar.supplementaries.client.tooltip;

import net.mehvahdjukaar.supplementaries.common.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SelectableContainerTooltip implements ClientTooltipComponent {

    private final SelectableContainerContent<?> contents;

    public SelectableContainerTooltip(SelectableContainerContent<?> content) {
        this.contents = content;
    }

    @Override
    public int getHeight() {
        return this.gridSizeY() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(Font font) {
        return this.gridSizeX() * 18 + 2;
    }

    private int backgroundWidth() {
        return this.gridSizeX() * 18 + 2;
    }

    private int backgroundHeight() {
        return this.gridSizeY() * 20 + 2;
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        guiGraphics.blitSprite(ModTextures.QUIVER_TOOLTIP_BACKGROUND_SPRITE, mouseX, mouseY, this.backgroundWidth(), this.backgroundHeight());
        int k = 0;

        for(int l = 0; l < j; ++l) {
            for(int m = 0; m < i; ++m) {
                int n = mouseX + m * 18 + 1;
                int o = mouseY + l * 20 + 1;
                this.renderSlot(n, o, k++, guiGraphics, font);
            }
        }

    }

    private void renderSlot(int x, int y, int itemIndex, GuiGraphics guiGraphics, Font font) {
        List<ItemStack> items = this.contents.getContentUnsafe();
        if (itemIndex >= items.size()) {
            guiGraphics.blitSprite(ModTextures.QUIVER_TOOLTIP_SLOT_SPRITE, x, y, 0, 18, 20);
            return;
        } else {
            ItemStack itemStack = items.get(itemIndex);
            guiGraphics.blitSprite(ModTextures.QUIVER_TOOLTIP_SLOT_SPRITE, x, y, 0, 18, 20);
            guiGraphics.renderItem(itemStack, x + 1, y + 1, itemIndex);
            guiGraphics.renderItemDecorations(font, itemStack, x + 1, y + 1);
            if (itemIndex == contents.getSelectedSlot()) {
                AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, y + 1, 0);
            }

        }
    }

    private int gridSizeX() {
        return this.contents.getSize();
    }

    private int gridSizeY() {
        return 1;
    }

}

