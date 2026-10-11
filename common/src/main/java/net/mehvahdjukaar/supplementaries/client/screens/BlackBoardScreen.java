package net.mehvahdjukaar.supplementaries.client.screens;


import net.mehvahdjukaar.moonlight.api.misc.CircularList;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.DrawableBlackBoardButton;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.DyeBlackBoardButton;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetBlackboardPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BlackBoardScreen extends Screen {

    private static final MutableComponent CLEAR = Component.translatable("gui.supplementaries.blackboard.clear");
    private static final MutableComponent UNDO = Component.translatable("gui.supplementaries.blackboard.undo");
    private static final MutableComponent EDIT = Component.translatable("gui.supplementaries.blackboard.edit");

    private final BlackboardBlockTile tile;
    private final BlackboardBlockTile[][] connectedBoards;

    private final List<DrawableBlackBoardButton> buttons = new ArrayList<>();
    private DrawableBlackBoardButton[][] buttonsByPixel;

    private final Deque<List<Entry>> history = new CircularList<>(20);
    private List<Entry> currentHistoryStep = new ArrayList<>();
    private Button historyButton;
    private byte selectedColor = 1;


    private BlackBoardScreen(BlackboardBlockTile teBoard, BlackboardBlockTile[][] boards) {
        super(EDIT);
        this.tile = teBoard;
        this.connectedBoards = boards;
    }

    public static void open(BlackboardBlockTile sign) {
        BlackboardBlockTile[][] boards = CommonConfigs.Building.BLACKBOARD_CONNECTED_GUI.get() ?
                findConnectedBoards(sign) : new BlackboardBlockTile[][]{{sign}};
        Minecraft.getInstance().setScreen(new BlackBoardScreen(sign, boards));
    }

    private static BlackboardBlockTile[][] findConnectedBoards(BlackboardBlockTile clicked) {
        //get all around first
        BlackboardBlockTile[][] around = new BlackboardBlockTile[5][5];
        addConnectedBoards(around, 2, 2, clicked);

        int startC = 1;
        int startR = 1;
        for (int sc = 0; sc < 3; sc++) {
            for (int sr = 0; sr < 3; sr++) {
                if (countBoardsInWindow(around, sc, sr) > countBoardsInWindow(around, startC, startR)) {
                    startC = sc;
                    startR = sr;
                }
            }
        }

        int minC = 4, maxC = 0, minR = 4, maxR = 0;
        for (int c = startC; c < startC + 3; c++) {
            for (int r = startR; r < startR + 3; r++) {
                if (around[c][r] == null) continue;
                minC = Math.min(minC, c);
                maxC = Math.max(maxC, c);
                minR = Math.min(minR, r);
                maxR = Math.max(maxR, r);
            }
        }
        BlackboardBlockTile[][] boards = new BlackboardBlockTile[maxC - minC + 1][maxR - minR + 1];
        for (int c = minC; c <= maxC; c++) {
            System.arraycopy(around[c], minR, boards[c - minC], 0, maxR - minR + 1);
        }
        return boards;
    }

    private static void addConnectedBoards(BlackboardBlockTile[][] around, int c, int r, BlackboardBlockTile clicked) {
        if (c < 0 || r < 0 || c > 4 || r > 4 || around[c][r] != null) return;
        Direction facing = clicked.getDirection();
        BlockPos pos = clicked.getBlockPos().relative(facing.getCounterClockWise(), c - 2).above(2 - r);
        if (clicked.getLevel().getBlockEntity(pos) instanceof BlackboardBlockTile board &&
                board.getDirection() == facing && !board.isWaxed()) {
            around[c][r] = board;
            addConnectedBoards(around, c + 1, r, clicked);
            addConnectedBoards(around, c - 1, r, clicked);
            addConnectedBoards(around, c, r + 1, clicked);
            addConnectedBoards(around, c, r - 1, clicked);
        }
    }

    private static int countBoardsInWindow(BlackboardBlockTile[][] around, int startC, int startR) {
        int count = 0;
        for (int c = startC; c < startC + 3; c++) {
            for (int r = startR; r < startR + 3; r++) {
                if (around[c][r] != null) count++;
            }
        }
        return count;
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
        if (this.minecraft == null || this.minecraft.player == null) return false;
        for (BlackboardBlockTile[] column : this.connectedBoards) {
            for (BlackboardBlockTile board : column) {
                if (board != null && board.isRemoved()) return false;
            }
        }
        return this.tile.canBeUsedBy(this.tile.getBlockPos(), this.minecraft.player);
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
        // send new image to the server. client tiles already have it
        for (var column : this.connectedBoards) {
            for (var board : column) {
                if (board == null) continue;
                byte[][] pixels = new byte[16][16];
                for (int xx = 0; xx < 16; xx++) {
                    for (int yy = 0; yy < 16; yy++) {
                        pixels[xx][yy] = board.getPixel(xx, yy);
                    }
                }
                NetworkHelper.sendToServer(new ServerBoundSetBlackboardPacket(board.getBlockPos(), pixels));
            }
        }
    }

    //dynamic refreshTextures for client
    public void updateBlackboard(int u, int v, byte newColor) {
        this.connectedBoards[u / 16][v / 16].setPixel(u % 16, v % 16, newColor);
    }

    public void addHistory(int u, int v, byte oldColor) {
        this.currentHistoryStep.add(new Entry(u, v, oldColor));
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
        for (DrawableBlackBoardButton b : this.buttons) {
            if (b.isMouseOver(mx, my) && b.getColor() != buttonValue)
                b.setColor(buttonValue);
        }
    }

    private void clearPressed(Button button) {
        for (DrawableBlackBoardButton b : this.buttons) {
            b.setColor((byte) 0);
        }
        this.saveHistoryStep();
    }

    private void undoPressed(Button button) {
        if (!this.history.isEmpty()) {
            for (var v : this.history.pollLast()) {
                this.buttonsByPixel[v.u()][v.v()].setColor(v.color());
            }
            //clear history step from this undo we just added
            this.currentHistoryStep.clear();
        }
        if (this.history.isEmpty()) {
            this.historyButton.active = false;
        }
    }

    @Override
    protected void init() {
        int columns = this.connectedBoards.length;
        int rows = this.connectedBoards[0].length;
        int pixelSize = Mth.clamp(Math.min((this.height / 4 + 51) / (rows * 16), (this.width - 80) / (columns * 16)),
                1, DrawableBlackBoardButton.MAX_SIZE);
        int left = this.width / 2 - columns * 8 * pixelSize;
        this.buttons.clear();
        this.buttonsByPixel = new DrawableBlackBoardButton[columns * 16][rows * 16];
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                BlackboardBlockTile board = this.connectedBoards[c][r];
                if (board == null) continue;
                for (int xx = 0; xx < 16; xx++) {
                    for (int yy = 0; yy < 16; yy++) {
                        int u = c * 16 + xx;
                        int v = r * 16 + yy;
                        var button = this.addRenderableWidget(new DrawableBlackBoardButton(this,
                                left + u * pixelSize, 65 + v * pixelSize, pixelSize, u, v, board.getPixel(xx, yy)));
                        this.buttons.add(button);
                        this.buttonsByPixel[u][v] = button;
                    }
                }
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
            for (byte b = 0; b < DyeColor.values().length; b++) {
                int ox = b / 8;
                int oy = b % 8;
                this.addRenderableWidget(new DyeBlackBoardButton(this, left - 16 +
                        -ox * 10 - ox * 2,
                        67 + oy * 10 + oy * 2, b));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, CommonColors.WHITE);

        // RenderSystem.enableDepthTest();
        super.render(graphics, mouseX, mouseY, partialTicks);
        // RenderSystem.disableDepthTest();
        graphics.pose().pushPose();
        for (DrawableBlackBoardButton button : this.buttons) {
            if (button.isShouldDrawOverlay()) {
                button.renderHoverOverlay(graphics);
                break;
            }
        }
        graphics.pose().popPose();
    }

    private record Entry(int u, int v, byte color) {
    }
}

