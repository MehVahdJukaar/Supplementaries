package net.mehvahdjukaar.supplementaries.client.renderers.forge;

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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class QuiverArrowSelectGuiImpl extends QuiverArrowSelectGui implements IGuiOverlay {

    public QuiverArrowSelectGuiImpl() {
        super(Minecraft.getInstance(),Minecraft.getInstance().getItemRenderer());
    }

    @Override
    protected void drawHighlight(PoseStack poseStack, int screenWidth, int py, ItemStack selectedArrow) {
            int l;

            MutableComponent mutablecomponent = Component.empty().append(selectedArrow.getHoverName()).withStyle(selectedArrow.getRarity().getStyleModifier());
            if (selectedArrow.hasCustomHoverName()) {
                mutablecomponent.withStyle(ChatFormatting.ITALIC);
            }
            Component highlightTip = selectedArrow.getHighlightTip(mutablecomponent);
            int fontWidth = this.getFont().width(highlightTip);
            int nx = (screenWidth - fontWidth) / 2;
            int ny = py - 19;

            l = 255;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Gui.fill(poseStack, nx - 2, ny - 2, nx + fontWidth + 2, ny + 9 + 2, this.minecraft.options.getBackgroundColor(0));
            Font font = IClientItemExtensions.of(selectedArrow).getFont(selectedArrow, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
            if (font == null) {
                this.getFont().drawShadow(poseStack, highlightTip, (float) nx, ny, 0xFFFFFF + (l << 24));
            } else {
                nx = (screenWidth - font.width(highlightTip)) / 2;
                font.drawShadow(poseStack, highlightTip, (float) nx, ny, 0xFFFFFF + (l << 24));
            }
            RenderSystem.disableBlend();
        }

    @Override
    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTicks, int width, int height) {
        if(isActive()) {
            renderQuiverContent(poseStack, partialTicks, width, height);
        }
    }

}
