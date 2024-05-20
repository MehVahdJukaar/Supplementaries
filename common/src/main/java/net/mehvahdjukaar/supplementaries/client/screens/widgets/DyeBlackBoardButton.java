package net.mehvahdjukaar.supplementaries.client.screens.widgets;


import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;


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
        graphics.blit(ModTextures.BLACKBOARD_BLACK_TEXTURE,
                this.x, this.y,
                (float) 0,0,
                size, size, 8, 8);
    }

}

