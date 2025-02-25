package net.mehvahdjukaar.supplementaries.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundCycleSelectableContainerItemPacket;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;


public abstract class SelectableContainerItemHud implements LayeredDraw.Layer {

    //deadlock prevention
    private static class Holder {
        private static final SelectableContainerItemHud INSTANCE = makeInstance();
    }

    public static SelectableContainerItemHud getInstance() {
        return Holder.INSTANCE;
    }

    @ExpectPlatform
    public static SelectableContainerItemHud makeInstance() {
        throw new AssertionError();
    }

    protected final Minecraft mc;
    //behold states
    @Nullable
    private SelectableContainerItem<?, ?> itemUsed;
    private SlotReference stackSlot;
    private boolean usingKey = false; //false if just using
    private double lastCumulativeMouseDx = 0;


    protected SelectableContainerItemHud(Minecraft minecraft) {
        this.mc = minecraft;
    }

    public boolean isActive() {
        return itemUsed != null;
    }

    public boolean isUsingKey() {
        return itemUsed != null && usingKey;
    }

    public boolean isUsingItem() {
        return itemUsed != null && !usingKey;
    }

    //todo: test key and use combinaton
    public void setUsingItem(SlotReference slot, LivingEntity player) {
        stackSlot = slot;
        if (slot.getItem(player) instanceof SelectableContainerItem<?, ?> selectable) {
            itemUsed = selectable;
        } else {
            itemUsed = null;
        }
    }

    public void setUsingKeybind(SlotReference slot, Player player) {
        setUsingItem(slot, player);
        usingKey = itemUsed != null;
    }

    private void closeHud() {
        itemUsed = null;
        usingKey = false;
        stackSlot = SlotReference.EMPTY;
    }

    @EventCalled
    public boolean onMouseScrolled(double scrollDelta) {
        if (itemUsed != null) {
            int amount = scrollDelta > 0 ? -1 : 1;
            sendCycle(amount);
            return true;
        }
        return false;
    }

    @EventCalled
    public void ohMouseMoved(double deltaX) {
        if (itemUsed != null && ClientConfigs.Items.QUIVER_MOUSE_MOVEMENT.get()) {

            double scale = mc.options.sensitivity().get() * 0.02;
            int oldI = (int) (lastCumulativeMouseDx * scale);
            lastCumulativeMouseDx += deltaX;
            int slotsMoved = (int) (lastCumulativeMouseDx * scale) - oldI;
            if (slotsMoved != 0) {
                Player player = mc.player;
                if (player != null) {
                    sendCycle(slotsMoved);
                }
            }
        }
    }

    private void sendCycle(int slotsMoved) {
        ItemStack stack = getItemUsed();
        if (!stack.isEmpty() && itemUsed != null) {
            NetworkHelper.sendToServer(new ServerBoundCycleSelectableContainerItemPacket(slotsMoved, stackSlot));
            //update client immediately. stacks now may be desynced
            itemUsed.modify(stack, m -> {
                m.cycle(slotsMoved);
                return true;
            });
        }
    }

    private void sendSetSlot(int number) {
        ItemStack stack = getItemUsed();
        if (!stack.isEmpty() && itemUsed != null) {
            NetworkHelper.sendToServer(new ServerBoundCycleSelectableContainerItemPacket(
                    number, stackSlot, true));
            itemUsed.modify(stack, m -> {
                m.setSelectedSlot(number);
                return true;
            });
        }
    }


    @EventCalled
    public boolean onKeyPressed(int key, int action, int modifiers) {
        if (itemUsed == null) return false;
        if (action != GLFW.GLFW_PRESS) return false;

        switch (key) {
            case GLFW.GLFW_KEY_LEFT -> {
                sendCycle(-1);
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                sendCycle(1);
                return true;
            }
        }
        int number = key - 48;
        if (number >= 1 && number <= 9) {
            if (number <= itemUsed.getMaxSlots()) {
                sendSetSlot(number - 1);
            }
            //cancels all number keys to prevent switching items
            return true;
        }
        return false;
    }

    @NotNull
    private ItemStack getItemUsed() {
        var player = mc.player;
        if (itemUsed == null) return ItemStack.EMPTY;
        ItemStack stack = stackSlot.get(player);
        if (!stack.is(itemUsed)) return ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker arg2) {
        if (itemUsed == null) return;
        if (!(mc.getCameraEntity() instanceof IQuiverPlayer)) {
            closeHud();
            return;
        }
        //checks for keypress here to handle all possible cases
        if (isUsingKey()) {
            if (!ClientRegistry.QUIVER_KEYBIND.isUnbound()) {
                boolean keyDown = InputConstants.isKeyDown(
                        mc.getWindow().getWindow(),
                        ClientRegistry.QUIVER_KEYBIND.key.getValue()
                );
                if (!keyDown) {
                    closeHud();
                    return;
                }
            }
        }

        ItemStack stack = getItemUsed();
        if (stack.isEmpty() ||! (stack.getItem() instanceof SelectableContainerItem<?,?> sc)) {
            closeHud();
            return;
        }
        var data = stack.get(sc.getComponentType());
        if (data == null) {
            closeHud();
            return;
        }

        ///gui.setupOverlayRenderState(true, false);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        int selected = data.getSelectedSlot();
        List<ItemStack> items = data.getContentUnsafe();
        int slots = items.size();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int centerX = graphics.guiWidth() / 2;

        poseStack.pushPose();
        poseStack.translate(0, 0, -90);

        int uWidth = slots * 20 + 2;
        int px = uWidth / 2;
        int py = graphics.guiHeight() / 2 - 40;

        px += ClientConfigs.Items.QUIVER_GUI_X.get();
        py += ClientConfigs.Items.QUIVER_GUI_Y.get();

        graphics.blitSprite(ModTextures.SELECTABLE_ITEM_BAR, 182, 22, 0, 0, centerX - px, py, uWidth - 1, 22);
        graphics.blitSprite(ModTextures.SELECTABLE_ITEM_BAR, 182, 22, 181, 0, centerX + px - 1, py,1, 22);
        graphics.blitSprite(ModTextures.SELECTABLE_ITEM_OVERLAY, centerX - px - 1 + selected * 20, py - 1,  24, 24);

        poseStack.popPose();

        int i1 = 1;

        for (int i = 0; i < slots; ++i) {
            int kx = centerX - px + 3 + i * 20;
            renderSlot(graphics, kx, py + 3, items.get(i), i1++, mc.font);
        }
        RenderSystem.disableBlend();


        ItemStack selectedArrow = items.get(selected);
        if (!selectedArrow.isEmpty()) {
            drawHighlight(graphics, graphics.guiWidth(), py, selectedArrow);
        }
        poseStack.popPose();
    }


    private void renderSlot(GuiGraphics graphics, int pX, int pY, ItemStack pStack, int seed, Font font) {
        if (!pStack.isEmpty()) {
            graphics.renderItem(pStack, pX, pY, seed);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            graphics.renderItemDecorations(font, pStack, pX, pY);
        }
    }


    protected abstract void drawHighlight(GuiGraphics graphics, int screenWidth, int py, ItemStack selectedArrow);


}