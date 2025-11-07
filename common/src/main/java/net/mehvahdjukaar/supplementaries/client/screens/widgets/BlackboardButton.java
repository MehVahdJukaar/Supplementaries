package net.mehvahdjukaar.supplementaries.client.screens.widgets;


import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;


public abstract class BlackboardButton implements GuiEventListener, Renderable, NarratableEntry {
    protected final BlackBoardScreen parent;
    private final ResourceLocation outlineTexture;
    public final int size;
    public final int x;
    public final int y;
    protected boolean shouldDrawOverlay;
    protected byte color;
    protected boolean focused;

    public BlackboardButton(BlackBoardScreen screen, int x, int y, byte color, int size,
                            ResourceLocation outlineTexture) {
        this.x = x;
        this.y = y;
        this.parent = screen;
        this.color = color;
        this.size = size;
        this.outlineTexture = outlineTexture;
    }

    public byte getColor() {
        return color;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.shouldDrawOverlay = this.isMouseOver(mouseX, mouseY);

        renderButton(graphics);

        if (this.isShouldDrawOverlay()) {
            renderHoverOverlay(graphics);
        }
    }

    protected abstract void renderButton(GuiGraphics graphics);

    public void renderHoverOverlay(GuiGraphics graphics) {
        var pose = graphics.pose();
        pose.pushPose();

        pose.translate(0, 0, 90);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        graphics.blitSprite(this.outlineTexture,
                this.x - 1, this.y - 1, size + 2, size + 2);

        pose.popPose();
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

    public boolean isShouldDrawOverlay() {
        return this.shouldDrawOverlay;
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
        return mouseX >= this.x && mouseY >= this.y && mouseX < (this.x + size) && mouseY < (this.y + size);
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

