package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.FenceSignBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.SignPostBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.UpdateServerTextHolderPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.fonts.TextInputUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.stream.IntStream;


public class FenceSignGui extends Screen {
    private TextInputUtil textInputUtil;
    /** The index of the line that is being edited. */
    private int editLine;
    //for ticking cursor
    private int updateCounter;
    private final FenceSignBlockTile tileSign;
    private static final int MAXLINES = FenceSignBlockTile.LINES;
    private final String[] cachedLines;
    public FenceSignGui(FenceSignBlockTile teSign) {
        super(new TranslationTextComponent("sign.edit"));
        this.tileSign = teSign;
        this.cachedLines = IntStream.range(0, MAXLINES).mapToObj(teSign.textHolder::getText).map(ITextComponent::getString).toArray(String[]::new);

    }

    public static void open(FenceSignBlockTile sign) {
        Minecraft.getInstance().setScreen(new FenceSignGui(sign));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.textInputUtil.charTyped(codePoint);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scrollText((int) delta);
        return true;
    }

    public void scrollText(int amount){
        this.editLine = Math.floorMod(this.editLine - amount, MAXLINES);
        this.textInputUtil.setCursorToEnd();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // up arrow
        if (keyCode == 265) {
            this.scrollText(1);
            return true;
        }
        // down arrow, enter
        else if(keyCode == 264 || keyCode == 257 || keyCode == 335) {
            this.scrollText(-1);
            return true;
        }
        return this.textInputUtil.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        ++this.updateCounter;
        if (!this.tileSign.getType().isValid(this.tileSign.getBlockState().getBlock())) {
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
        // send new text to the server
        NetworkHandler.INSTANCE.sendToServer(new UpdateServerTextHolderPacket(this.tileSign.getBlockPos(), this.tileSign.textHolder.signText, this.tileSign.textHolder.lines));
        //this.tileSign.textHolder.setEditable(true);
    }

    private void close() {
        this.tileSign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, DialogTexts.GUI_DONE, (p_238847_1_) -> this.close()));
        //this.tileSign.textHolder.setEditable(false);
        this.textInputUtil = new TextInputUtil(() -> this.cachedLines[this.editLine], (p_238850_1_) -> {
            this.cachedLines[this.editLine] = p_238850_1_;
            this.tileSign.textHolder.setText(this.editLine, new StringTextComponent(p_238850_1_));
        }, TextInputUtil.createClipboardGetter(this.minecraft), TextInputUtil.createClipboardSetter(this.minecraft), (p_238848_1_) -> this.minecraft.font.width(p_238848_1_) <= 90);
    }


    @Override

    public void render(MatrixStack matrixstack, int mouseX, int mouseY, float partialTicks) {
        RenderHelper.setupForFlatItems();
        this.renderBackground(matrixstack);
        drawCenteredString(matrixstack, this.font, this.title, this.width / 2, 40, 16777215);

        IRenderTypeBuffer.Impl irendertypebuffer$impl = this.minecraft.renderBuffers().bufferSource();
        matrixstack.pushPose();
        matrixstack.translate(this.width / 2d, 0.0D, 50.0D);

        matrixstack.scale(93.75F, -93.75F, 93.75F);
        matrixstack.translate(0.0D, -1.3125D, 0.0D);
        // renders sign
        matrixstack.pushPose();
        //matrixstack.scale(0.6666667F, 0.6666667F, 0.6666667F);
        //matrixstack.rotate(Const.Y90);

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();


        //render signs


        matrixstack.pushPose();

        matrixstack.scale(1,-1,-1);
        RenderMaterial material = Materials.BELLOWS_MATERIAL;
        IVertexBuilder builder =  material.buffer(irendertypebuffer$impl, RenderType::entitySolid);
        SignPostBlockTileRenderer.signModel.render(matrixstack, builder, 15728880, OverlayTexture.NO_OVERLAY);


        matrixstack.popPose();



        //render fence
        matrixstack.translate(-0.5, -0.5, -0.5);
        BlockState fence = this.tileSign.fenceBlock;
        if(fence !=null)blockRenderer.renderBlock(fence, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

        matrixstack.popPose();

        //renders text
        boolean flag1 = this.updateCounter / 6 % 2 == 0;


        matrixstack.translate(0, 0.21875, 0.1875 + 0.005);
        matrixstack.scale(0.010416667F, -0.010416667F, 0.010416667F);

        Matrix4f matrix4f = matrixstack.last().pose();

        int i = this.tileSign.textHolder.textColor.getTextColor();
        int j = this.textInputUtil.getCursorPos();
        int k = this.textInputUtil.getSelectionPos();
        //int i1 = this.minecraft.fontRenderer.getBidiFlag() ? -1 : 1;
        int l = this.editLine * 48 - this.tileSign.textHolder.lines * 5;

        for(int i1 = 0; i1 < this.cachedLines.length; ++i1) {
            String s = this.cachedLines[i1];
            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }
                float f3 = (float) (-this.minecraft.font.width(s) / 2) -3;
                //this.minecraft.fontRenderer.renderString(s, f3, (float) (k1 * 48 - this.tileSign.signText.length * 5), i, false, matrix4f,
                 //       irendertypebuffer$impl, false, 0, 15728880); //*10
                this.minecraft.font.drawInBatch(s, f3, (float)(i1 * 48 - this.cachedLines.length * 5), i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880, false);
                if (i1 == this.editLine && j >= 0 && flag1) {
                    int j1 = this.minecraft.font.width(s.substring(0, Math.max(Math.min(j, s.length()), 0)));

                    int k1 = (-3 + j1 - this.minecraft.font.width(s) / 2);
                    if (j >= s.length()) {
                        this.minecraft.font.drawInBatch("_", (float)k1, (float)l, i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880, false);
                    }
                }
            }
        }


        irendertypebuffer$impl.endBatch();
        //draw highlighted text box

        for(int i3 = 0; i3 < this.cachedLines.length; ++i3) {
            String s1 = this.cachedLines[i3];
            if (s1 != null && i3 == this.editLine && j >= 0) {
                int j3 = this.minecraft.font.width(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
                int k3 = -3 + j3 - this.minecraft.font.width(s1) / 2;
                if (flag1 && j < s1.length()) {
                    fill(matrixstack, k3, l - 1, k3 + 1, l + 9, -16777216 | i);
                }

                if (k != j) {
                    int l3 =  Math.min(j, k);
                    int l1 =  Math.max(j, k);

                    int i2 = this.minecraft.font.width(s1.substring(0, l3)) - this.minecraft.font.width(s1) / 2;
                    int j2 = this.minecraft.font.width(s1.substring(0, l1)) - this.minecraft.font.width(s1) / 2;
                    int k2 = -3 + Math.min(i2, j2);
                    int l2 = -3 + Math.max(i2, j2);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuilder();
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    bufferbuilder.vertex(matrix4f, (float)k2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float)l2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float)l2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float)k2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.end();
                    WorldVertexBufferUploader.end(bufferbuilder);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        matrixstack.popPose();
        RenderHelper.setupFor3DItems();
        super.render(matrixstack, mouseX, mouseY, partialTicks);
    }
}