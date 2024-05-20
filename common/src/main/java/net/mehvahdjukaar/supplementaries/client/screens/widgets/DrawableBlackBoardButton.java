package net.mehvahdjukaar.supplementaries.client.screens.widgets;


import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;


public class DrawableBlackBoardButton extends BlackboardButton {

    public static final int SIZE = 6;

    private final int u;
    private final int v;

    public DrawableBlackBoardButton(BlackBoardScreen screen, int centerX, int centerY, int u, int v, byte color) {
        super(screen, centerX - ((8 - u) * SIZE), centerY - ((-v) * SIZE), color, SIZE);
        this.u = u;
        this.v = v;
    }

    public void setColor(byte color) {
        this.parent.addHistory(this.u, this.v, this.color);
        this.color = color;
        this.parent.updateBlackboard(this.u, this.v, color);
    }


    @Override
    protected void onClick() {
        byte selectedColor = parent.getSelectedColor();
        setColor(selectedColor == this.color ? 0 : selectedColor);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isValidClickButton(button)) {
            this.parent.onButtonDragged(mouseX, mouseY, this.color);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            this.parent.saveHistoryStep();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void renderButton(GuiGraphics graphics) {
        int offset = this.color > 0 ? 16 : 0;
        graphics.blit(ModTextures.BLACKBOARD_GUI_TEXTURE,
                this.x, this.y,
                (float) (this.u + offset) * size, (float) this.v * size,
                size, size, 32 * size, 16 * size);
    }

}

