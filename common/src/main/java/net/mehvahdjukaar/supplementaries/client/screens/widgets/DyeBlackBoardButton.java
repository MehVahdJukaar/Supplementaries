package net.mehvahdjukaar.supplementaries.client.screens.widgets;


import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;


public class DyeBlackBoardButton extends BlackboardButton {

    public static final int SIZE = 8;

    public DyeBlackBoardButton(BlackBoardScreen screen, int centerX, int centerY, int u, int v, byte color) {
        super(screen, centerX - ((8 - u) * SIZE), centerY - ((-v) * SIZE), 0, 0, color, SIZE);
    }

    public void setColor(byte color) {
        this.parent.addHistory(this.u, this.v, this.color);
        this.color = color;
        this.parent.updateBlackboard(this.u, this.v, color);
    }


    @Override
    protected void onClick() {
        parent.setSelectedColor(this.color);
    }

}

