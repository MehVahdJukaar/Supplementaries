package net.mehvahdjukaar.supplementaries.client.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.SelectableContainerItemHud;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class SelectableContainerItemHudImpl implements IGuiOverlay {

    protected Minecraft minecraft;

    public SelectableContainerItemHudImpl() {
        this.minecraft = Minecraft.getInstance();
    }

    public static void drawHighlight(Minecraft mc, GuiGraphics graphics, int screenWidth, int py, ItemStack selectedArrow) {
        int l;

        MutableComponent mutablecomponent = Component.empty().append(selectedArrow.getHoverName()).withStyle(selectedArrow.getRarity().getStyleModifier());
        if (selectedArrow.hasCustomHoverName()) {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }
        Component highlightTip = selectedArrow.getHighlightTip(mutablecomponent);
        int fontWidth = mc.font.width(highlightTip);
        int nx = (screenWidth - fontWidth) / 2;
        int ny = py - 19;

        l = 255;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.fill(nx - 2, ny - 2, nx + fontWidth + 2, ny + 9 + 2, mc.options.getBackgroundColor(0));
        Font font = IClientItemExtensions.of(selectedArrow).getFont(selectedArrow, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
        if (font == null) {
            graphics.drawString(mc.font, highlightTip, nx, ny, 0xFFFFFF + (l << 24));
        } else {
            nx = (screenWidth - font.width(highlightTip)) / 2;
            graphics.drawString(font, highlightTip, nx, ny, 0xFFFFFF + (l << 24));
        }
        RenderSystem.disableBlend();
    }


    @Override
    public void render(ForgeGui forgeGui, GuiGraphics arg, float f, int i, int j) {
        SelectableContainerItemHud.render(minecraft, arg, f, i, j);
    }

}
