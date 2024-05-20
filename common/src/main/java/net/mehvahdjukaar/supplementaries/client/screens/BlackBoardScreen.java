package net.mehvahdjukaar.supplementaries.client.screens;


import net.mehvahdjukaar.supplementaries.client.screens.widgets.DrawableBlackBoardButton;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.DyeBlackBoardButton;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetBlackboardPacket;
import net.mehvahdjukaar.supplementaries.common.utils.CircularList;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.ImmediatelyFastCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BlackBoardScreen extends Screen {

    private static final MutableComponent CLEAR = Component.translatable("gui.supplementaries.blackboard.clear");
    private static final MutableComponent UNDO = Component.translatable("gui.supplementaries.blackboard.undo");
    private static final MutableComponent EDIT = Component.translatable("gui.supplementaries.blackboard.edit");

    private final BlackboardBlockTile tile;

    private final DrawableBlackBoardButton[][] buttons = new DrawableBlackBoardButton[16][16];

    private final Deque<List<Entry>> history = new CircularList<>(20);
    private List<Entry> currentHistoryStep = new ArrayList<>();
    private Button historyButton;
    private byte selectedColor = 1;


    private record Entry(int x, int y, byte color) {
    }

    private BlackBoardScreen(BlackboardBlockTile teBoard) {
        super(EDIT);
        this.tile = teBoard;
    }

    public static void open(BlackboardBlockTile sign) {
        Minecraft.getInstance().setScreen(new BlackBoardScreen(sign));
    }

    @Override
    public void tick() {
        if (!isValid()) {
            this.onClose();
        } else {
            if (!(this.getFocused() instanceof DrawableBlackBoardButton)) {
                setFocused(null); //dont focus clear buttons
            }
        }
    }

    private boolean isValid() {
        return this.minecraft != null && this.minecraft.player != null && !this.tile.isRemoved() &&
                !this.tile.playerIsTooFarAwayToEdit(tile.getLevel(), tile.getBlockPos(), this.minecraft.player.getUUID());
    }

    public byte getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(byte color) {
        this.selectedColor = color;
    }


    @Override
    public void onClose() {
        this.tile.setChanged();
        super.onClose();
    }

    @Override
    public void removed() {
        // send new image to the server
        byte[][] pixels = new byte[16][16];
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                pixels[xx][yy] = (this.buttons[xx][yy].getColor());
            }
        }
        ModNetwork.CHANNEL.sendToServer(new ServerBoundSetBlackboardPacket(this.tile.getBlockPos(), pixels));
    }

    //dynamic refreshTextures for client
    public void updateBlackboard(int x, int y, byte newColor) {
        this.tile.setPixel(x, y, newColor);
    }

    public void addHistory(int x, int y, byte oldColor) {
        this.currentHistoryStep.add(new Entry(x, y, oldColor));
    }

    public void saveHistoryStep() {
        if (!currentHistoryStep.isEmpty()) {
            this.history.add(currentHistoryStep);
            this.currentHistoryStep = new ArrayList<>();
            this.historyButton.active = true;
        }
    }

    //calls drag for other buttons
    public void onButtonDragged(double mx, double my, byte buttonValue) {
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                DrawableBlackBoardButton b = this.buttons[xx][yy];
                if (b.isMouseOver(mx, my) && b.getColor() != buttonValue)
                    b.setColor(buttonValue);
            }
        }
    }

    private void clearPressed(Button button) {
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                this.buttons[xx][yy].setColor((byte) 0);
            }
        }
        this.saveHistoryStep();
    }


    private void undoPressed(Button button) {
        if (!this.history.isEmpty()) {
            for (var v : this.history.pollLast()) {
                this.buttons[v.x()][v.y()].setColor(v.color());
            }
            ;
            //clear history step from this undo we just added
            this.currentHistoryStep.clear();
        }
        if (this.history.isEmpty()) {
            this.historyButton.active = false;
        }
    }


    @Override
    protected void init() {
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                byte pixel = this.tile.getPixel(xx, yy);
                DrawableBlackBoardButton widget = new DrawableBlackBoardButton(this, (this.width / 2), 40 + 25, xx, yy, pixel);
                this.buttons[xx][yy] = this.addRenderableWidget(widget);
            }
        }

        int buttonW = 56;
        int sep = 4;
        this.addRenderableWidget(Button.builder(CLEAR, this::clearPressed)
                .bounds(this.width / 2 - buttonW / 2 - buttonW + sep / 2, this.height / 4 + 120, buttonW - sep, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - buttonW / 2 + sep / 2, this.height / 4 + 120, buttonW - sep, 20).build());

        this.historyButton = this.addRenderableWidget(Button.builder(UNDO, this::undoPressed)
                .bounds(this.width / 2 + buttonW / 2 + sep / 2, this.height / 4 + 120, buttonW - sep, 20).build());

        if (CommonConfigs.Building.BLACKBOARD_COLOR.get()) {
            for (byte b = 0; b < 16; b++) {
                int ox = b / 8;
                int oy = b % 8;
                this.addRenderableWidget(new DyeBlackBoardButton(this, this.width / 2 - 80 + ox * 10,
                        73 + oy * 10, b));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);

        if (CompatHandler.IMMEDIATELY_FAST) ImmediatelyFastCompat.startBatching();

        // RenderSystem.enableDepthTest();
        super.render(graphics, mouseX, mouseY, partialTicks);
        // RenderSystem.disableDepthTest();
        graphics.pose().pushPose();
        label:
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                DrawableBlackBoardButton button = this.buttons[xx][yy];
                if (button.isShouldDrawOverlay()) {
                    button.renderHoverOverlay(graphics);
                    break label;
                }
            }
        }
        graphics.pose().popPose();
        if (CompatHandler.IMMEDIATELY_FAST) ImmediatelyFastCompat.endBatching();
    }
}

