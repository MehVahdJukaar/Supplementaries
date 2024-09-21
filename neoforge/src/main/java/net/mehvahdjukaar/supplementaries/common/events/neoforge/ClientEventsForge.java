package net.mehvahdjukaar.supplementaries.common.events.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.SoundEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

public class ClientEventsForge {

    public static void init() {
        NeoForge.EVENT_BUS.register(ClientEventsForge.class);
    }

    private static boolean hasOptifine;
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
            ClientEvents.onItemTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
        }
    }


    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (CompatHandler.CONFIGURED) {
            ClientEvents.addConfigButton(event.getScreen(), event.getListenersList(), event::addListener);
        }
    }

    @SubscribeEvent
    public static void onClientEndTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientEvents.onClientTick(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (Minecraft.getInstance().screen == null &&
                ClientRegistry.QUIVER_KEYBIND.matches(event.getKey(), event.getScanCode())
                && Minecraft.getInstance().player instanceof IQuiverPlayer qe) {
            int a = event.getAction();
            if (a == InputConstants.REPEAT || a == InputConstants.PRESS) {
                SelectableContainerItemHud.INSTANCE.setUsingKeybind(qe.supplementaries$getQuiverSlot());
            } else if (a == InputConstants.RELEASE) {
                SelectableContainerItemHud.INSTANCE.setUsingKeybind(SlotReference.EMPTY);
            }
        }

        if (CannonController.isActive()) {
            CannonController.onKeyPressed(event.getKey(), event.getAction(), event.getModifiers());
            //event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        if (SelectableContainerItemHud.INSTANCE.onMouseScrolled(event.getScrollDelta())) {
            event.setCanceled(true);
        }
        if (CannonController.isActive()) {
            CannonController.onMouseScrolled(event.getScrollDelta());
            event.setCanceled(true);
        }
    }


    //forge only below

    //TODO: add to fabric

    private static double wobble; // from 0 to 1

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Player p = Minecraft.getInstance().player;
        if (p != null && !Minecraft.getInstance().isPaused()) {
            boolean isOnRope = ClientEvents.isIsOnRope();
            if (isOnRope || wobble != 0) {
                double period = ClientConfigs.Blocks.ROPE_WOBBLE_PERIOD.get();
                double newWobble = (((p.tickCount + event.getPartialTick()) / period) % 1);
                if (!isOnRope && newWobble < wobble) {
                    wobble = 0;
                } else {
                    wobble = newWobble;
                }
                event.setRoll((float) (event.getRoll() + Mth.sin((float) (wobble * 2 * Math.PI)) * ClientConfigs.Blocks.ROPE_WOBBLE_AMPLITUDE.get()));
            }
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
    public static void onSoundPlay(SoundEvent.SoundSourceEvent event) {
        SongsManager.recordNoteFromSound(event.getSound(), event.getName());
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        Item i = stack.getItem();
        var pattern = DecoratedPotPatterns.getResourceKey(i);
        if (pattern != null && i != Items.BRICK) {
            event.getTooltipElements().add(Either.right(new SherdTooltip(pattern)));
        }
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (CannonController.isActive()) {
            event.setCanceled(true);
            event.setSwingHand(false);
            if (event.isAttack()) {
                CannonController.onPlayerAttack();
            } else if (event.isUseItem()) {
                CannonController.onPlayerUse();
            }
        }
    }

    @SubscribeEvent
    public static void renderHandEvent(RenderHandEvent event) {
        if (CannonController.isActive())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (CannonController.isActive()) {
            var overlay = event.getOverlay();
            if (overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type() || overlay == VanillaGuiOverlay.HOTBAR.type()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOutline(RenderHighlightEvent.Block event) {
        if (CannonController.isActive()) {
            event.setCanceled(true);
        }
    }


}
