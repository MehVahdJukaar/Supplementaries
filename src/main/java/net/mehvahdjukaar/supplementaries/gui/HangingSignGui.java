package net.mehvahdjukaar.supplementaries.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.HangingSignBlockTile;
import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.network.UpdateServerHangingSignPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.fonts.TextInputUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class HangingSignGui extends Screen {
    private TextInputUtil textInputUtil;
    // The index of the line that is being edited.
    private int editLine = 0;
    //for ticking cursor
    private int updateCounter;
    private final HangingSignBlockTile tileSign;
    private static final int MAXLINES = 5;
    private final String[] cachedLines;

    private BlackBoardButton rem;

    public HangingSignGui(HangingSignBlockTile teSign) {
        super(new TranslationTextComponent("sign.edit"));
        this.tileSign = teSign;
        this.cachedLines = IntStream.range(0, MAXLINES).mapToObj(teSign::getText).map(ITextComponent::getString).toArray(String[]::new);
    }

    public static void open(HangingSignBlockTile sign) {
        Minecraft.getInstance().displayGuiScreen(new HangingSignGui(sign));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.textInputUtil.putChar(codePoint);
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scrollText((int)delta);
        return true;
    }

    public void scrollText(int amount){
        this.editLine = Math.floorMod(this.editLine - amount, MAXLINES);
        this.textInputUtil.moveCursorToEnd();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // up arrow
        if (keyCode == 265) {
            this.scrollText(1);
            return true;
        }
        // !down arrow, !enter, !enter, handles special keys
        else if (keyCode != 264 && keyCode != 257 && keyCode != 335) {
            return this.textInputUtil.specialKeyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        // down arrow, enter
        else {
            this.scrollText(-1);
            return true;
        }
    }

    @Override
    public void tick() {
        ++this.updateCounter;
        if (!this.tileSign.getType().isValidBlock(this.tileSign.getBlockState().getBlock())) {
            this.close();
        }
    }


    @Override
    public void closeScreen() {
        this.close();
    }

    @Override
    public void onClose() {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
        // send new text to the server
        Networking.INSTANCE.sendToServer(new UpdateServerHangingSignPacket(this.tileSign.getPos(), this.tileSign.getText(0), this.tileSign.getText(1),
                this.tileSign.getText(2), this.tileSign.getText(3), this.tileSign.getText(4)));
        
        this.tileSign.setEditable(true);
    }

    private void close() {
        this.tileSign.markDirty();
        this.minecraft.displayGuiScreen(null);
    }

    @Override
    protected void init() {

        rem = new BlackBoardButton(188, this.height/3, 8, 8);
        this.addListener(rem);

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, DialogTexts.GUI_DONE, (p_238847_1_) -> this.close()));
        this.tileSign.setEditable(false);
        this.textInputUtil = new TextInputUtil(() -> this.cachedLines[this.editLine], (p_238850_1_) -> {
            this.cachedLines[this.editLine] = p_238850_1_;
            this.tileSign.setText(this.editLine, new StringTextComponent(p_238850_1_));
        }, TextInputUtil.getClipboardTextSupplier(this.minecraft), TextInputUtil.getClipboardTextSetter(this.minecraft), (p_238848_1_) -> this.minecraft.fontRenderer.getStringWidth(p_238848_1_) <= 75);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, int  mouseX, int mouseY, float partialTicks) {
        RenderHelper.setupGuiFlatDiffuseLighting();
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        MatrixStack matrixstack = new MatrixStack();
        IRenderTypeBuffer.Impl irendertypebuffer$impl = this.minecraft.getRenderTypeBuffers().getBufferSource();
        matrixstack.push();
        matrixstack.translate((double) (this.width / 2), 0.0D, 50.0D);





        matrixstack.scale(93.75F, -93.75F, 93.75F);
        //rem.render(matrixStack,mouseX,mouseY,partialTicks);


        matrixstack.translate(0.0D, -1.3125D, 0.0D);
        // renders sign
        matrixstack.push();
        // matrixstack.scale(0.6666667F, 0.6666667F, 0.6666667F);
        matrixstack.rotate(Vector3f.YP.rotationDegrees(90));
        matrixstack.translate(0, - 0.5 + 0.1875, -0.5);
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = this.tileSign.getBlockState().getBlock().getDefaultState().with(HangingSignBlock.TILE, true);
        blockRenderer.renderBlock(state, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        matrixstack.pop();



        //renders text
        boolean flag1 = this.updateCounter / 6 % 2 == 0;

        matrixstack.translate(0, 0, 0.0625 + 0.005);
        matrixstack.scale(0.010416667F, -0.010416667F, 0.010416667F);

        Matrix4f matrix4f = matrixstack.getLast().getMatrix();

        int i = this.tileSign.getTextColor().getTextColor();
        int j = this.textInputUtil.getEndIndex();
        int k = this.textInputUtil.getStartIndex();
        int l = this.editLine * 10 - this.tileSign.signText.length * 5;

        for(int i1 = 0; i1 < this.cachedLines.length; ++i1) {
            String s = this.cachedLines[i1];
            if (s != null) {
                if (this.font.getBidiFlag()) {
                    s = this.font.bidiReorder(s);
                }
                float f3 = (float) (-this.minecraft.fontRenderer.getStringWidth(s) / 2);
                //this.minecraft.fontRenderer.renderString(s, f3, (float) (k1 * 48 - this.tileSign.signText.length * 5), i, false, matrix4f,
                //       irendertypebuffer$impl, false, 0, 15728880); //*10
                this.minecraft.fontRenderer.func_238411_a_(s, f3, (float)(i1 * 10 - this.cachedLines.length * 5), i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880, false);
                if (i1 == this.editLine && j >= 0 && flag1) {
                    int j1 = this.minecraft.fontRenderer.getStringWidth(s.substring(0, Math.max(Math.min(j, s.length()), 0)));

                    int k1 = ( j1 - this.minecraft.fontRenderer.getStringWidth(s) / 2);
                    if (j >= s.length()) {
                        this.minecraft.fontRenderer.func_238411_a_("_", (float)k1, (float)l, i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880, false);
                    }
                }
            }
        }



        irendertypebuffer$impl.finish();
        //draw highlighted text box

        for(int i3 = 0; i3 < this.cachedLines.length; ++i3) {
            String s1 = this.cachedLines[i3];
            if (s1 != null && i3 == this.editLine && j >= 0) {
                int j3 = this.minecraft.fontRenderer.getStringWidth(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
                int k3 = j3 - this.minecraft.fontRenderer.getStringWidth(s1) / 2;
                if (flag1 && j < s1.length()) {
                    fill(matrixStack, k3, l - 1, k3 + 1, l + 9, -16777216 | i);
                }

                if (k != j) {
                    int l3 = Math.min(j, k);
                    int l1 = Math.max(j, k);

                    int i2 = this.minecraft.fontRenderer.getStringWidth(s1.substring(0, l3)) - this.minecraft.fontRenderer.getStringWidth(s1) / 2;
                    int j2 = this.minecraft.fontRenderer.getStringWidth(s1.substring(0, l1)) - this.minecraft.fontRenderer.getStringWidth(s1) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    bufferbuilder.pos(matrix4f, (float)k2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.pos(matrix4f, (float)l2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.pos(matrix4f, (float)l2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.pos(matrix4f, (float)k2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.finishDrawing();
                    WorldVertexBufferUploader.draw(bufferbuilder);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }
        matrixstack.pop();
        RenderHelper.setupGui3DDiffuseLighting();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}

