package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.inventories.SackContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;



 public class SackGui extends AbstractContainerScreen<SackContainer> {

    public SackGui(SackContainer container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
    }

    private void renderBack(PoseStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(Textures.SACK_GUI_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
    }


    private void renderSlots(PoseStack matrixStack){

        Minecraft.getInstance().getTextureManager().bind(Textures.SLOT_TEXTURE);

        int k = -1+(this.width - this.imageWidth) / 2;
        int l = -1+(this.height - this.imageHeight) / 2;

        int size = ServerConfigs.cached.SACK_SLOTS;



        int[] dims = SackContainer.getRatio(size);
        if(dims[0]>9){
            dims[0] = 9;
            dims[1] = (int) Math.ceil(size/9f);
        }

        int yp = 17 +(18*3)/2 - (9)*dims[1];

        int dimx;
        int xp;
        for(int h = 0; h < dims[1]; ++h) {
            dimx = Math.min(dims[0],size);
            xp = 8+ (18*9)/2 -(dimx*18)/2;
            for (int j = 0; j < dimx; ++j) {
                blit(matrixStack, k + xp + j * 18, l + yp+18*h, 0, 0, 18, 18, 18, 18);
            }
            size-=dims[0];
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.renderBack(matrixStack,partialTicks,mouseX,mouseY);
        this.renderSlots(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
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
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }
}

