package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientReceivers;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundCycleQuiverPacket;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundCycleQuiverPacket.Slot;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public abstract class QuiverArrowSelectGui extends Gui {
    private static final ResourceLocation TEXTURE = Supplementaries.res("textures/gui/quiver_select.png");

    //behold states
    private static boolean usingItem;
    private static double lastCumulativeMouseDx = 0;
    private static boolean usingKey = false;

    public static boolean isActive() {
        return usingItem || usingKey;
    }

    public static void setUsingItem(boolean on) {
        if (on != usingItem) lastCumulativeMouseDx = 0;
        usingItem = on;
    }

    public static boolean isUsingKey() {
        return usingKey;
    }

    public static void setUsingKeybind(boolean on) {
        if(on)  Minecraft.getInstance().player.displayClientMessage(Component.literal("Keybind mode!"),true);

        if (on != usingItem) lastCumulativeMouseDx = 0;
        usingKey = on;
    }

    protected final ItemRenderer itemRenderer;
    protected final Minecraft minecraft;

    protected QuiverArrowSelectGui(Minecraft minecraft, ItemRenderer itemRenderer) {
        super(minecraft, itemRenderer);
        this.itemRenderer = itemRenderer;
        this.minecraft = minecraft;
    }



    public static void ohMouseMoved(double deltaX) {
        if(!usingKey) Minecraft.getInstance().player.displayClientMessage(Component.literal("Move your mouse to select!"),true);
        double scale = Minecraft.getInstance().options.sensitivity().get() * 0.02;
        int oldI = (int) (lastCumulativeMouseDx * scale);
        lastCumulativeMouseDx += deltaX;
        int slotsMoved = (int) (lastCumulativeMouseDx * scale) - oldI;
        if (slotsMoved != 0) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                Slot s = getQuiverSlot(player);
                NetworkHandler.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(slotsMoved, s));
            }
        }
    }

    @EventCalled
    public static boolean onMouseScrolled(double scrollDelta) {
        if(!usingKey) Minecraft.getInstance().player.displayClientMessage(Component.literal("...or scroll"),true);

        Player player = Minecraft.getInstance().player;
        NetworkHandler.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(
                scrollDelta > 0 ? -1 : 1, getQuiverSlot(player)));
        return true;
    }

    @EventCalled
    public static boolean onKeyPressed(int key, int action, int modifiers) {
        if(!usingKey) Minecraft.getInstance().player.displayClientMessage(Component.literal("or use a number key"),true);

        //maybe add key thing here
        if (action == 1) {
            Player player = Minecraft.getInstance().player;

            switch (key) {
                case 263 -> { //left arrow;
                    NetworkHandler.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(
                            -1, getQuiverSlot(player)));
                    return true;
                }
                case 262 -> { //right arrow;
                    NetworkHandler.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(
                            1, getQuiverSlot(player)));
                    return true;
                }
            }
            int number = key - 48;
            if (number >= 1 && number <= 9) {
                if (number <= CommonConfigs.Items.QUIVER_SLOTS.get()) {
                    NetworkHandler.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(
                            number - 1, getQuiverSlot(player), true));
                }
                //cancels all number keys to prevent switching items
                return true;
            }
        }
        return false;
    }

    private void renderSlot(int pX, int pY, Player pPlayer, ItemStack pStack, int seed) {
        if (!pStack.isEmpty()) {
            this.itemRenderer.renderAndDecorateItem(pPlayer, pStack, pX, pY, seed);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, pStack, pX, pY);
        }
    }

    public void renderQuiverContent(PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight) {

        if (minecraft.getCameraEntity() instanceof Player player) {
            ItemStack quiver = getCurrentlyUsedQuiver(player);
            if (quiver.getItem() == ModRegistry.QUIVER_ITEM.get()) {
                ///gui.setupOverlayRenderState(true, false);

                poseStack.pushPose();

                var data = QuiverItem.getQuiverData(quiver);

                int selected = data.getSelectedSlot();
                List<ItemStack> items = data.getContentView();
                int slots = items.size();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, TEXTURE);

                int centerX = screenWidth / 2;
                int z = this.getBlitOffset();
                this.setBlitOffset(-90);
                int uWidth = slots * 20 + 2;
                int px = uWidth / 2;
                int py = screenHeight / 2 - 40;

                px += ClientConfigs.Items.QUIVER_GUI_X.get();
                py += ClientConfigs.Items.QUIVER_GUI_Y.get();

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
                if (!selectedArrow.isEmpty()) {
                    drawHighlight(poseStack, screenWidth, py, selectedArrow);
                }

                poseStack.popPose();

                setUsingItem(true);
                return;
            }
        }
        setUsingItem(false);

    }


    @NotNull
    private static Slot getQuiverSlot(Player player) {
        return usingKey ? Slot.INVENTORY : (player.getUsedItemHand() == InteractionHand.MAIN_HAND ? Slot.MAIN_HAND : Slot.OFF_HAND);
    }

    private static ItemStack getCurrentlyUsedQuiver(Player player) {
        if (usingKey) {
            return ((IQuiverEntity) player).getQuiver();
        }
        return player.getUseItem();
    }

    protected abstract void drawHighlight(PoseStack poseStack, int screenWidth, int py, ItemStack selectedArrow);


}