package net.mehvahdjukaar.supplementaries.client.screens;

import net.mehvahdjukaar.supplementaries.common.inventories.PulleyContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;


public class PulleyScreen extends AbstractContainerScreen<PulleyContainerMenu> {

    private final CyclingSlotBackground slotBG = new CyclingSlotBackground(0);

    public PulleyScreen(PulleyContainerMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        graphics.blit(ModTextures.PULLEY_BLOCK_GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.slotBG.render(this.menu, graphics, partialTicks, this.leftPos, this.topPos);
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
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.slotBG.tick(ModTextures.PULLEY_SLOT_ICONS);
    }
}