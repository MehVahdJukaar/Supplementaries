package net.mehvahdjukaar.supplementaries.client.screens;

import net.mehvahdjukaar.supplementaries.common.inventories.VariableSizeContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class VariableSizeContainerScreen extends AbstractContainerScreen<VariableSizeContainerMenu> {

    private final ResourceLocation backgroundTexture;

    public VariableSizeContainerScreen(VariableSizeContainerMenu container, Inventory inventory, Component text,
                                       ResourceLocation backgroundTexture) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.backgroundTexture = backgroundTexture;
    }

    @Deprecated(forRemoval = true)
    public VariableSizeContainerScreen(VariableSizeContainerMenu container, Inventory inventory, Component text) {
        this(container, inventory, text, ModTextures.SACK_GUI_TEXTURE);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(backgroundTexture, x, y, 0, 0,
                this.imageWidth, this.imageHeight);
        this.renderSlots(graphics);
    }

    private void renderSlots(GuiGraphics graphics) {
        int k = -1 + this.leftPos;
        int l = -1 + this.topPos;

        int size = this.menu.unlockedSlots;

        int[] dims = VariableSizeContainerMenu.getRatio(size);
        if (dims[0] > 9) {
            dims[0] = 9;
            dims[1] = (int) Math.ceil(size / 9f);
        }

        int yp = 17 + (18 * 3) / 2 - (9) * dims[1];

        int dimx;
        int xp;
        for (int h = 0; h < dims[1]; ++h) {
            dimx = Math.min(dims[0], size);
            xp = 8 + (18 * 9) / 2 - (dimx * 18) / 2;
            for (int j = 0; j < dimx; ++j) {
                graphics.blitSprite(ModTextures.SLOT_SPRITE, k + xp + j * 18, l + yp + 18 * h,
                        18,18);
            }
            size -= dims[0];
        }
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }
}

