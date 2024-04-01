package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundCycleQuiverPacket;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundCycleQuiverPacket.Slot;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class QuiverArrowSelectGui extends Gui {
    private static final ResourceLocation TEXTURE = Supplementaries.res("textures/gui/quiver_select.png");

    //behold states
    private static boolean usingItem;
    private static boolean usingKeyAndHasItem = false;

    private static double lastCumulativeMouseDx = 0;

    public static boolean isActive() {
        return usingItem || usingKeyAndHasItem;
    }

    public static void setUsingItem(boolean on) {
        usingItem = on;
    }

    public static boolean isUsingKey() {
        return usingKeyAndHasItem;
    }

    public static void setUsingKeybind(boolean on) {
        //if (on != usingItem) lastCumulativeMouseDx = 0;
        usingKeyAndHasItem = on && (Minecraft.getInstance().player instanceof IQuiverEntity qe && qe.supplementaries$hasQuiver());
    }

    protected final Minecraft minecraft;

    protected QuiverArrowSelectGui(Minecraft minecraft, ItemRenderer itemRenderer) {
        super(minecraft, itemRenderer);
        this.minecraft = minecraft;
    }


    public static void onPlayerRotated(double yRotIncrease) {
        int slotsMoved = (int) (yRotIncrease*0.2);
        if (slotsMoved != 0) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                Slot s = getQuiverSlot(player);
                ModNetwork.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(slotsMoved, s));
            }
        }
    }
    @EventCalled
    public static void ohMouseMoved(double deltaX) {
        double scale = Minecraft.getInstance().options.sensitivity().get() * 0.02;
        int oldI = (int) (lastCumulativeMouseDx * scale);
        lastCumulativeMouseDx += deltaX;
        int slotsMoved = (int) (lastCumulativeMouseDx * scale) - oldI;
        if (slotsMoved != 0) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                Slot s = getQuiverSlot(player);
                ModNetwork.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(slotsMoved, s));
            }
        }
    }

    @EventCalled
    public static boolean onKeyPressed(int key, int action, int modifiers) {
        //maybe add key thing here
        if (action == 1) {
            Player player = Minecraft.getInstance().player;

            switch (key) {
                case 263 -> { //left arrow;
                    ModNetwork.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(
                            -1, getQuiverSlot(player)));
                    return true;
                }
                case 262 -> { //right arrow;
                    ModNetwork.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(
                            1, getQuiverSlot(player)));
                    return true;
                }
            }
            int number = key - 48;
            if (number >= 1 && number <= 9) {
                if (number <= CommonConfigs.Tools.QUIVER_SLOTS.get()) {
                    ModNetwork.CHANNEL.sendToServer(new ServerBoundCycleQuiverPacket(
                            number - 1, getQuiverSlot(player), true));
                }
                //cancels all number keys to prevent switching items
                return true;
            }
        }
        return false;
    }

    private void renderSlot(GuiGraphics graphics, int pX, int pY, ItemStack pStack, int seed) {
        if (!pStack.isEmpty()) {
            graphics.renderItem(pStack, pX, pY, seed);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            graphics.renderItemDecorations(this.minecraft.font, pStack, pX, pY);
        }
    }

    public void renderQuiverContent(GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight) {

        if (minecraft.getCameraEntity() instanceof Player player) {
            ItemStack quiver = getCurrentlyUsedQuiver(player);
            if (quiver.getItem() == ModRegistry.QUIVER_ITEM.get()) {
                ///gui.setupOverlayRenderState(true, false);
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();

                var data = QuiverItem.getQuiverData(quiver);

                int selected = data.getSelectedSlot();
                List<ItemStack> items = data.getContentView();
                int slots = items.size();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                int centerX = screenWidth / 2;

                poseStack.pushPose();
                poseStack.translate(0, 0, -90);

                int uWidth = slots * 20 + 2;
                int px = uWidth / 2;
                int py = screenHeight / 2 - 40;

                px += ClientConfigs.Items.QUIVER_GUI_X.get();
                py += ClientConfigs.Items.QUIVER_GUI_Y.get();

                graphics.blit(TEXTURE, centerX - px, py, 0, 0, uWidth - 1, 22);
                graphics.blit(TEXTURE, centerX + px - 1, py, 0, 0, 1, 22);
                graphics.blit(TEXTURE, centerX - px - 1 + selected * 20, py - 1, 24, 22, 24, 24);

                poseStack.popPose();

                int i1 = 1;

                for (int i = 0; i < slots; ++i) {
                    int kx = centerX - px + 3 + i * 20;
                    this.renderSlot(graphics, kx, py + 3, items.get(i), i1++);
                }
                RenderSystem.disableBlend();


                ItemStack selectedArrow = items.get(selected);
                if (!selectedArrow.isEmpty()) {
                    drawHighlight(graphics, screenWidth, py, selectedArrow);
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
        return usingKeyAndHasItem ? Slot.INVENTORY : (player.getUsedItemHand() == InteractionHand.MAIN_HAND ? Slot.MAIN_HAND : Slot.OFF_HAND);
    }

    private static ItemStack getCurrentlyUsedQuiver(Player player) {
        if (usingKeyAndHasItem) {
            return ((IQuiverEntity) player).supplementaries$getQuiver();
        }
        return player.getUseItem();
    }

    protected abstract void drawHighlight(GuiGraphics graphics, int screenWidth, int py, ItemStack selectedArrow);


}