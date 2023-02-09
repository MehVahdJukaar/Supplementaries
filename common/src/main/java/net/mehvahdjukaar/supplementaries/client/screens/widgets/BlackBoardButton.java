package net.mehvahdjukaar.supplementaries.client.screens.widgets;


import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;


public class BlackBoardButton extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public final int u;
    public final int v;
    public final int x;
    public final int y;
    public static final int WIDTH = 6;
    protected boolean isHovered;
    protected byte color = 0;
    protected boolean focused;

    private final IDraggable onDragged;
    private final IPressable onPress;

    public BlackBoardButton(int centerX, int centerY, int u, int v, IPressable pressedAction,
                            IDraggable dragAction) {
        this.x = centerX - ((8 - u) * WIDTH);
        this.y = centerY - ((-v) * WIDTH);
        this.u = u;
        this.v = v;
        this.onPress = pressedAction;
        this.onDragged = dragAction;
    }

    public void setColor(byte color) {
        this.color = color;
    }

    public byte getColor() {
        return color;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.isHovered = this.isMouseOver(mouseX, mouseY);
        this.renderButton(matrixStack);
        //soboolean wasHovered = this.isHovered();
    }


    public void renderButton(PoseStack matrixStack) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ModTextures.BLACKBOARD_GUI_TEXTURE);

        int offset = this.color > 0 ? 16 : 0;

        int rgb = BlackboardBlock.colorFromByte(this.color);
        float b = NativeImage.getR(rgb) / 255f;
        float g = NativeImage.getG(rgb) / 255f;
        float r = NativeImage.getB(rgb) / 255f;

        RenderSystem.setShaderColor(r, g, b, 1.0F);
        blit(matrixStack, this.x, this.y, (float)(this.u + offset) * WIDTH, (float)this.v * WIDTH, WIDTH, WIDTH, 32 * WIDTH, 16 * WIDTH);
    }

    public void renderTooltip(PoseStack matrixStack) {
        //maybe remove this
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1);

        blit(matrixStack, this.x - 1, this.y - 1, 16f * WIDTH, 0, WIDTH + 2, WIDTH + 2, 32 * WIDTH, 16 * WIDTH);
        this.renderButton(matrixStack);
    }

    //toggle
    public void onClick(double mouseX, double mouseY) {
        this.color = (byte) (this.color == 0 ? 1 : 0);
        this.onPress.onPress(this.u, this.v, this.color != 0);

    }

    public void onRelease(double mouseX, double mouseY) {
    }

    //set
    public void onDrag(double mouseX, double mouseY, boolean on) {
        this.color = (byte) (on ? 1 : 0);
        this.onPress.onPress(this.u, this.v, this.color != 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            boolean flag = this.isMouseOver(mouseX, mouseY);
            if (flag) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onClick(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            this.onRelease(mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

        if (this.isValidClickButton(button)) {
            this.onDragged.onDragged(mouseX, mouseY, this.color != 0);
            return true;
        } else {
            return false;
        }
    }

    public boolean isHovered() {
        return this.isHovered || this.focused;
    }

    @Override
    public boolean changeFocus(boolean focus) {
        this.focused = !this.focused;
        //this.onFocusedChanged(this.focused);
        return this.focused;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >=  this.x && mouseY >=  this.y && mouseX <  (this.x + WIDTH) && mouseY <  (this.y + WIDTH);
    }


    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public interface IPressable {
        void onPress(int x, int y, boolean on);
    }

    public interface IDraggable {
        void onDragged(double mouseX, double mouseY, boolean on);
    }

}

