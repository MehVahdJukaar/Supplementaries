package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class QuiverArrowSelectGui extends Gui {
    static final ResourceLocation TEXTURE = Supplementaries.res("textures/gui/quiver_select.png");

    private static boolean active;
    private static double lastCumulativeMouseDx = 0;

    protected final ItemRenderer itemRenderer;
    protected final Minecraft minecraft;

    public static boolean isActive() {
        return active;
    }

    public QuiverArrowSelectGui(Minecraft minecraft, ItemRenderer itemRenderer) {
        super(minecraft, itemRenderer);
        this.itemRenderer = itemRenderer;
        this.minecraft = minecraft;
    }

    public static void setActive(boolean on) {
        if (on != active) lastCumulativeMouseDx = 0;
        active = on;
    }

    public static void ohMouseMoved(double deltaX) {
        double scale =  Minecraft.getInstance().options.sensitivity().get() * 0.02;
        int oldI = (int) (lastCumulativeMouseDx * scale);
        lastCumulativeMouseDx += deltaX;
        int slotsMoved =  (int)(lastCumulativeMouseDx * scale) - oldI;
        if (slotsMoved != 0) {
            Player player = Minecraft.getInstance().player;
            ItemStack quiver = player.getUseItem();
            QuiverItem.getQuiverData(quiver).cycle(slotsMoved);
        }
    }

    public static boolean onKeyPressed(int key, int action, int modifiers) {
        if (action == 1) {
            Player player = Minecraft.getInstance().player;
            ItemStack quiver = player.getUseItem();
            QuiverItem.IQuiverData data = QuiverItem.getQuiverData(quiver);
            switch (key) {
                case 263 -> {
                    data.cycle(-1);//left arrow;
                    return true;
                }
                case 262 -> {
                    data.cycle(1);//right arrow;
                    return true;
                }
            }
            int number = key - 48;
            if (number >= 1 && number <= 9) {
                if(number <= CommonConfigs.Items.QUIVER_SLOTS.get()) {
                    data.setSelectedSlot(number);
                }
                //cancels all number keys to prevent switching items
                return true;
            }
        }
        return false;
    }

/*
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        !InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)
        if (pKeyCode == 293 && this.currentlyHovered.isPresent()) {
            this.setFirstMousePos = false;
            this.currentlyHovered = this.currentlyHovered.get().getNext();
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }*/

    private void renderSlot(int pX, int pY, Player pPlayer, ItemStack pStack, int seed) {
        if (!pStack.isEmpty()) {
            this.itemRenderer.renderAndDecorateItem(pPlayer, pStack, pX, pY, seed);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, pStack, pX, pY);
        }
    }

    public void renderQuiverContent(PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight) {

        if (minecraft.getCameraEntity() instanceof Player player) {
            ItemStack quiver = player.getUseItem();
            if (quiver.getItem() == ModRegistry.QUIVER_ITEM.get()) {
                ///gui.setupOverlayRenderState(true, false);

                poseStack.pushPose();

                var data = QuiverItem.getQuiverData(quiver);
                //renderQuiverItems(poseStack, data.getContent(), data.getSelectedSlot());


                int selected = data.getSelectedSlot();
                List<ItemStack> items = data.getContent();
                int slots = items.size();

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, TEXTURE);

                int centerX = screenWidth / 2;
                int z = this.getBlitOffset();
                this.setBlitOffset(-90);
                int uWidth = slots * 20 + 2;
                int px = uWidth / 2;
                int py = screenHeight / 2 - 40;

                this.blit(poseStack, centerX - px, py, 0, 0, uWidth - 1, 22);
                this.blit(poseStack, centerX + px - 1, py, 0, 0, 1, 22);
                this.blit(poseStack, centerX - px - 1 + selected * 20, py - 1, 24, 22, 24, 24);


                this.setBlitOffset(z);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                int i1 = 1;

                for (int i = 0; i < slots; ++i) {
                    int kx = centerX - px + 3 + i * 20;
                    this.renderSlot(kx, py + 3, player, items.get(i), i1++);
                }
                RenderSystem.disableBlend();


                ItemStack selectedArrow = items.get(selected);
                drawHighlight(poseStack, screenWidth, py, selectedArrow);


                poseStack.popPose();

                setActive(true);
                return;
            }
        }
        setActive(false);

    }

    protected abstract void drawHighlight(PoseStack poseStack, int screenWidth, int py, ItemStack selectedArrow);


}