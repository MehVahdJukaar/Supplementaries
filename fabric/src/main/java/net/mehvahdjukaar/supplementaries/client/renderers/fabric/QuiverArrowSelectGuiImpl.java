package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class QuiverArrowSelectGuiImpl extends QuiverArrowSelectGui {

    public static QuiverArrowSelectGuiImpl INSTANCE = new QuiverArrowSelectGuiImpl();

    public QuiverArrowSelectGuiImpl() {
        super(Minecraft.getInstance(), Minecraft.getInstance().getItemRenderer());
    }

    @Override
    protected void drawHighlight(PoseStack poseStack, int screenWidth, int py, ItemStack selectedArrow) {
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
        Gui.fill(poseStack, nx - 2, ny - 2, nx + fontWidth + 2, ny + 9 + 2, this.minecraft.options.getBackgroundColor(0));
        Font font = Minecraft.getInstance().font;
        if (font == null) {
            this.getFont().drawShadow(poseStack, highlightTip, (float) nx, ny, 0xFFFFFF + (l << 24));
        } else {
            nx = (screenWidth - font.width(highlightTip)) / 2;
            font.drawShadow(poseStack, highlightTip, (float) nx, ny, 0xFFFFFF + (l << 24));
        }
        RenderSystem.disableBlend();
    }


    public void render(PoseStack poseStack, float partialTicks) {
        if(isActive()) {
            renderQuiverContent(poseStack, partialTicks,
                    Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                    Minecraft.getInstance().getWindow().getGuiScaledHeight());
        }
    }

}
