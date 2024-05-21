package net.mehvahdjukaar.supplementaries.client.screens.widgets;


import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;


public class DyeBlackBoardButton extends BlackboardButton {

    public static final int SIZE = 10;

    public DyeBlackBoardButton(BlackBoardScreen screen, int x, int y, byte color) {
        super(screen, x, y,  color, SIZE);
    }

    @Override
    protected void onClick() {
        parent.setSelectedColor(this.color);
    }


    @Override
    protected void renderButton(GuiGraphics graphics) {
        int rgb = this.color == 0 ? DyeColor.BLACK.getMapColor().col : BlackboardBlock.colorFromByte(this.color);
        float mul = shouldDrawOverlay ? 1.2f : 1.0f;
        float b = Mth.clamp(FastColor.ARGB32.blue(rgb) / 255f * mul,0,1);
        float r = Mth.clamp(FastColor.ARGB32.red(rgb) / 255f * mul,0,1);
        float g = Mth.clamp(FastColor.ARGB32.green(rgb) / 255f * mul,0,1);

        RenderSystem.setShaderColor(r, g, b, 1.0F);
        graphics.blit(ModTextures.BLACKBOARD_BLANK_TEXTURE,
                this.x, this.y,
                (float) 0,0,
                size, size, 8, 8);
        this.shouldDrawOverlay = parent.getSelectedColor() == this.color;
    }

}

