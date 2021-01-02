package net.mehvahdjukaar.supplementaries.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlackBoardButton extends AbstractGui implements IRenderable, IGuiEventListener {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(Supplementaries.MOD_ID+":textures/gui/blackboard.png");
    protected int width;
    protected int height;
    public int x;
    public int y;
    private boolean wasHovered;
    protected boolean isHovered;
    public boolean active = true;
    private boolean focused;



    public BlackBoardButton(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected int getYImage(boolean isHovered) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (isHovered) {
            i = 2;
        }
        return i;
    }

    public int getHeightRealms() {
        return this.height;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        this.renderButton(matrixStack, mouseX, mouseY, partialTicks);

        this.wasHovered = this.isHovered();

    }


    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);

        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(0.0F, 1.0F, 1.0F, 2);
        blit(matrixStack, 19, 19, 0, 0, 16, 16,16,16);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 2);
        blit(matrixStack, 20, 20, 0, 0, 16, 16,16,16);
    }



    public void onClick(double mouseX, double mouseY) {
        this.active=!this.active;
    }

    public void onRelease(double mouseX, double mouseY) {
    }

    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.clicked(mouseX, mouseY);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }

        }
        return false;
    }

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

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isValidClickButton(button)) {
            this.onDrag(mouseX, mouseY, dragX, dragY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height);
    }

    public boolean isHovered() {
        return this.isHovered || this.focused;
    }

    public boolean changeFocus(boolean focus) {
        if (this.active) {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    protected void onFocusedChanged(boolean focused) {
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height);
    }


    public void playDownSound(SoundHandler handler) {
        handler.play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

}

