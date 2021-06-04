package net.mehvahdjukaar.supplementaries.client.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.UpdateServerBlackboardPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.text.TranslationTextComponent;


public class BlackBoardGui extends Screen {
    private final BlackboardBlockTile tileBoard;

    private final BlackBoardButton[][] buttons = new BlackBoardButton[16][16];


    public BlackBoardGui(BlackboardBlockTile teBoard) {
        super(new TranslationTextComponent("gui.supplementaries.blackboard.edit"));
        this.tileBoard = teBoard;

    }

    public static void open(BlackboardBlockTile sign) {
        Minecraft.getInstance().setScreen(new BlackBoardGui(sign));
    }


    @Override
    public void tick() {
        if (!this.tileBoard.getType().isValid(this.tileBoard.getBlockState().getBlock())) {
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
        for (int xx=0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                pixels[xx][yy] = (this.buttons[xx][yy].color);
            }
        }
        NetworkHandler.INSTANCE.sendToServer(new UpdateServerBlackboardPacket(this.tileBoard.getBlockPos(),pixels));
    }

    private void close() {

        this.tileBoard.setChanged();
        this.minecraft.setScreen(null);
    }

    //dynamic update for client
    public void setPixel(int x, int y, boolean on){
        this.tileBoard.pixels[x][y]= (byte) (on?1:0);
    }

    //calls drag for other buttons
    public void dragButtons(double mx, double my, boolean on){
        for (int xx=0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                if(this.buttons[xx][yy].isMouseOver(mx,my))
                    this.buttons[xx][yy].onDrag(mx,my,on);
            }
        }
    }

    private void clear(){
        for (int xx=0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                setPixel(xx,yy,false);
                this.buttons[xx][yy].color = 0;
            }
        }
    }

    @Override
    protected void init() {
        for (int xx=0; xx < 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                this.buttons[xx][yy]=new BlackBoardButton((this.width / 2), 40 + 25, xx, yy, this::setPixel, this::dragButtons);
                this.addWidget(this.buttons[xx][yy]);
                this.buttons[xx][yy].color=this.tileBoard.pixels[xx][yy];
            }
        }

        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 100-4, 20, new TranslationTextComponent("gui.supplementaries.blackboard.clear"), (b) -> this.clear()));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 120, 100-4, 20, DialogTexts.GUI_DONE, (p_238847_1_) -> this.close()));
    }

    @Override
    public void render(MatrixStack matrixstack, int  mouseX, int mouseY, float partialTicks) {
        RenderHelper.setupForFlatItems();
        this.renderBackground(matrixstack);
        drawCenteredString(matrixstack, this.font, this.title, this.width / 2, 40, 16777215);


        matrixstack.pushPose();
        //float ff = 93.75F/16f;
        //matrixstack.scale(ff,ff,ff);
        int ut =-1;
        int vt =-1;
        for (int xx=0; xx< 16; xx++) {
            for (int yy = 0; yy < 16; yy++) {
                if(this.buttons[xx][yy].isHovered()){
                    ut=xx;
                    vt=yy;
                }
                this.buttons[xx][yy].render(matrixstack, mouseX, mouseY, partialTicks);
            }
        }
        if(ut!=-1)this.buttons[ut][vt].renderTooltip(matrixstack);
        matrixstack.popPose();

        //TODO: could be optimized a lot. too bad
        RenderHelper.setupFor3DItems();
        super.render(matrixstack, mouseX, mouseY, partialTicks);
    }
}

