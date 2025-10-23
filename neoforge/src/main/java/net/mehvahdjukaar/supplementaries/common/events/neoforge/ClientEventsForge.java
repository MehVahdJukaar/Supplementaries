package net.mehvahdjukaar.supplementaries.common.events.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.SherdTooltip;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

public class ClientEventsForge {

    public static void init() {
        NeoForge.EVENT_BUS.register(ClientEventsForge.class);
    }

    private static boolean firstScreenShown;

    @SubscribeEvent
    public static void onScreenDrawPost(ScreenEvent.Init.Post event) {
        if (!firstScreenShown && event.getScreen() instanceof TitleScreen) {
            ClientEvents.onFirstScreen(event.getScreen());
            firstScreenShown = true;
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() != null) {
            ClientEvents.onItemTooltip(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
        }
    }


    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (CompatHandler.CONFIGURED) {
            ClientEvents.addConfigButton(event.getScreen(), event.getListenersList(), event::addListener);
        }
    }

    @SubscribeEvent
    public static void onClientEndTick(ClientTickEvent.Post event) {
        ClientEvents.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        int action = event.getAction();
        if (mc.screen == null &&
                ClientRegistry.QUIVER_KEYBIND.matches(event.getKey(), event.getScanCode())
                && mc.player instanceof IQuiverPlayer qe) {
            if (action == InputConstants.REPEAT || action == InputConstants.PRESS) {
                SelectableContainerItemHud.getInstance().setUsingKeybind(qe.supplementaries$getQuiverSlot(), mc.player);
            } else if (action == InputConstants.RELEASE) {
                SelectableContainerItemHud.getInstance().setUsingKeybind(SlotReference.EMPTY, mc.player);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        if (ClientEvents.onMouseScrolled(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && CannonController.onPlayerAttack()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        } else if (event.isUseItem() && CannonController.onPlayerUse()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void renderHandEvent(RenderHandEvent event) {
        if (CannonController.isActive()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiLayerEvent.Pre event) {
        if (CannonController.cancelsXPBar()) {
            var overlay = event.getName();
            if (overlay == (VanillaGuiLayers.EXPERIENCE_BAR)) {
                event.setCanceled(true);
            }
        }
        if (CannonController.cancelsHotBar()) {
            var overlay = event.getName();
            if (overlay == (VanillaGuiLayers.HOTBAR)) {
                event.setCanceled(true);
            }
        }
    }

    //forge only below

    //TODO: add to fabric


    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        double wobble = ClientEvents.getRopeWobble(event.getPartialTick());
        if (wobble != 0) {
            event.setRoll((float) (event.getRoll() + wobble));
        }
    }

    static boolean mutex = false;

    @SubscribeEvent
    public static void onPlayerDeath(ScreenEvent.Opening event) {
        if (!mutex && event.getNewScreen() instanceof DeathScreen && event.getCurrentScreen() instanceof ChatScreen cs
                && ClientConfigs.Tweaks.DEATH_CHAT.get()) {
            //in case some mod were to somehow open a death screen from methods below
            mutex = true;
            cs.charTyped((char) GLFW.GLFW_KEY_MINUS, 0);
            cs.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
            mutex = false;
        }
    }

    @SubscribeEvent
    public static void onSoundPlay(PlaySoundSourceEvent event) {
        SongsManager.recordNoteFromSound(event.getSound(), event.getName());
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        Item i = stack.getItem();
        var pattern = DecoratedPotPatterns.getPatternFromItem(i);
        if (pattern != null && i != Items.BRICK) {
            event.getTooltipElements().add(Either.right(new SherdTooltip(pattern)));
        }
    }

    @SubscribeEvent
    public static void onRenderOutline(RenderHighlightEvent.Block event) {
        if (CannonController.isActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAddTooltips(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof SelectableContainerItem<?, ?> si) {
            SelectableContainerContent<?> data = stack.get(si.getComponentType());
            if (data == null) return;
            ItemStack selected = data.getSelected();
            if (selected.getItem() instanceof SelectableContainerItem<?, ?>) {
                return;
            }
            RenderTooltipEvent.GatherComponents newEvent = new RenderTooltipEvent.GatherComponents(selected,
                    event.getScreenWidth(), event.getScreenHeight(), event.getTooltipElements(), event.getMaxWidth());
            NeoForge.EVENT_BUS.post(newEvent);
        }
    }


}
