package net.mehvahdjukaar.supplementaries.client.gui;


import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.BlackBoardButton;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetBlackboardPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;


public class BlackBoardGui extends Screen {
    private final BlackboardBlockTile tileBoard;

    private final BlackBoardButton[][] buttons = new BlackBoardButton[16][16];

    private BlackBoardGui(BlackboardBlockTile teBoard) {
        super(new TranslatableComponent("gui.supplementaries.blackboard.edit"));
        this.tileBoard = teBoard;
    }

    public static void open(BlackboardBlockTile sign) {
        Minecraft.getInstance().setScreen(new BlackBoardGui(sign));
    }

    @Override
    public void tick() {
        if (!this.tileBoard.getType().isValid(this.tileBoard.getBlockState())) {
            this.close();
        }
    }

    @Override
    public void onClose() {
        this.close();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        // send new image to the server
        byte[][] pixels = new byte[16][16];
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                pixels[xx][yy] = (this.buttons[xx][yy].color);
            }
        }
        NetworkHandler.INSTANCE.sendToServer(new ServerBoundSetBlackboardPacket(this.tileBoard.getBlockPos(), pixels));
    }

    private void close() {

        this.tileBoard.setChanged();
        this.minecraft.setScreen(null);
    }

    //dynamic refreshTextures for client
    public void setPixel(int x, int y, boolean on) {
        this.tileBoard.setPixel(x,y,(byte) (on ? 1 : 0));
    }

    //calls drag for other buttons
    public void dragButtons(double mx, double my, boolean on) {
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                if (this.buttons[xx][yy].isMouseOver(mx, my))
                    this.buttons[xx][yy].onDrag(mx, my, on);
            }
        }
    }

    private void clear() {
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                setPixel(xx, yy, false);
                this.buttons[xx][yy].color = 0;
            }
        }
    }

    @Override
    protected void init() {
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                this.buttons[xx][yy] = new BlackBoardButton((this.width / 2), 40 + 25, xx, yy, this::setPixel, this::dragButtons);
                this.addWidget(this.buttons[xx][yy]);
                this.buttons[xx][yy].color = this.tileBoard.getPixel(xx,yy);
            }
        }

        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 100 - 4, 20, new TranslatableComponent("gui.supplementaries.blackboard.clear"), (b) -> this.clear()));
        this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 120, 100 - 4, 20, CommonComponents.GUI_DONE, (p_238847_1_) -> this.close()));
    }

    @Override
    public void render(PoseStack matrixstack, int mouseX, int mouseY, float partialTicks) {
        Lighting.setupForFlatItems();
        this.renderBackground(matrixstack);
        drawCenteredString(matrixstack, this.font, this.title, this.width / 2, 40, 16777215);


        matrixstack.pushPose();
        //float ff = 93.75F/16f;
        //matrixstack.scale(ff,ff,ff);
        int ut = -1;
        int vt = -1;
        for (int xx = 0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                if (this.buttons[xx][yy].isHovered()) {
                    ut = xx;
                    vt = yy;
                }
                this.buttons[xx][yy].render(matrixstack, mouseX, mouseY, partialTicks);
            }
        }
        if (ut != -1) this.buttons[ut][vt].renderTooltip(matrixstack);
        matrixstack.popPose();

        Lighting.setupFor3DItems();
        super.render(matrixstack, mouseX, mouseY, partialTicks);
    }
}

