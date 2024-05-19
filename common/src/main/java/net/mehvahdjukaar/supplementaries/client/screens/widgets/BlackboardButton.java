package net.mehvahdjukaar.supplementaries.client.screens.widgets;


import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;


public abstract class  BlackboardButton implements GuiEventListener, Renderable, NarratableEntry {
public static final int SIZE = 6;
    protected final BlackBoardScreen parent;
    public final int size;
    public final int u;
    public final int v;
    public final int x;
    public final int y;
    protected boolean isHovered;
    protected byte color;
    protected boolean focused;

    public BlackboardButton(BlackBoardScreen screen, int x, int y, int u, int v,  byte color, int size) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.parent = screen;
        this.color = color;
        this.size = size;
    }

    public byte getColor() {
        return color;
    }

    @Override
    public void render(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks) {
        this.isHovered = this.isMouseOver(mouseX, mouseY);
        renderButton(poseStack);
        //boolean wasHovered = this.isHovered();
    }

    public void renderButton(GuiGraphics graphics) {
        int offset = this.color > 0 ? 16 : 0;

        int rgb = BlackboardBlock.colorFromByte(this.color);
        float b = FastColor.ARGB32.blue(rgb) / 255f;
        float g = FastColor.ARGB32.green(rgb) / 255f;
        float r = FastColor.ARGB32.red(rgb) / 255f;

        RenderSystem.setShaderColor(r, g, b, 1.0F);
        graphics.blit( ModTextures.BLACKBOARD_GUI_TEXTURE,this.x, this.y,
                (float) (this.u + offset) * SIZE, (float) this.v * SIZE,
                SIZE, SIZE, 32 * SIZE, 16 * SIZE);
    }

    public void renderTooltip(GuiGraphics poseStack) {
        //maybe remove this
        poseStack.pose().translate(0,0,90);
        RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1);

        poseStack. blit(ModTextures.BLACKBOARD_GUI_TEXTURE, this.x - 1, this.y - 1,
                16f * SIZE, 0,
                SIZE + 2, SIZE + 2, 32 * SIZE, 16 * SIZE);
        //render again to cover stuff
        this.renderButton(poseStack);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            boolean flag = this.isMouseOver(mouseX, mouseY);
            if (flag) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onClick();
                return true;
            }
        }
        return false;
    }

    protected abstract void onClick();

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < (this.x + SIZE) && mouseY < (this.y + SIZE);
    }


    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

}

