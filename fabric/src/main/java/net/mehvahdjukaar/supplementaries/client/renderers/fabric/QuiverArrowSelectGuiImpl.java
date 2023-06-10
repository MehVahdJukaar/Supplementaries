package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.ClothConfigDemo;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class QuiverArrowSelectGuiImpl extends QuiverArrowSelectGui {

    public static final QuiverArrowSelectGuiImpl INSTANCE = new QuiverArrowSelectGuiImpl();

    public QuiverArrowSelectGuiImpl() {
        super(Minecraft.getInstance(), Minecraft.getInstance().getItemRenderer());
    }

    @Override
    protected void drawHighlight(GuiGraphics graphics, int screenWidth, int py, ItemStack selectedArrow) {
        int l;
        MutableComponent mutablecomponent = Component.empty().append(selectedArrow.getHoverName()).withStyle(selectedArrow.getRarity().color);
        if (selectedArrow.hasCustomHoverName()) {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }
        Component highlightTip = selectedArrow.getHoverName();
        int fontWidth = this.getFont().width(highlightTip);
        int nx = (screenWidth - fontWidth) / 2;
        int ny = py - 19;

        l = 255;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.fill( nx - 2, ny - 2, nx + fontWidth + 2, ny + 9 + 2, this.minecraft.options.getBackgroundColor(0));
        Font font = this.getFont();
        nx = (screenWidth - font.width(highlightTip)) / 2;
        graphics.drawString(font, highlightTip, nx, ny, 0xFFFFFF + (l << 24));
        RenderSystem.disableBlend();
    }


    public void render(GuiGraphics graphics, float partialTicks) {
        if(isActive()) {
            renderQuiverContent(graphics, partialTicks,
                    Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                    Minecraft.getInstance().getWindow().getGuiScaledHeight());
        }
    }

}
