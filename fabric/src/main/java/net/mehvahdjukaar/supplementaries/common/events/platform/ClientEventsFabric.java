package net.mehvahdjukaar.supplementaries.common.events.platform;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeHud;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.client.hud.SlimedOverlayHud;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

public class ClientEventsFabric {

    public static void init() {
        ClientEntityEvents.ENTITY_LOAD.register(ClientEvents::onEntityLoad);

        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
        ScreenEvents.AFTER_INIT.register((m, s, x, y) -> {
            List<AbstractWidget> buttons = Screens.getButtons(s);
            ClientEvents.addConfigButton(s, s.children(), buttons::add, buttons::remove);
        });

        ClientPreAttackCallback.EVENT.register((minecraft, localPlayer, i) -> {
            if (CannonController.onPlayerAttack()) {
                return true;
            }
            return false;
        });


        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);


        HudRenderCallback.EVENT.register(ClientEventsFabric::onRenderHud);

    }

    private static void onRenderHud(GuiGraphics graphics, DeltaTracker partialTicks) {
        SelectableContainerItemHud.getInstance().render(graphics, partialTicks);
        SlimedOverlayHud.INSTANCE.render(graphics, partialTicks);
        CannonChargeHud.INSTANCE.render(graphics, partialTicks);
        //equivalent of forge event to check beybind. more efficent like this on forge
        Minecraft mc = Minecraft.getInstance();
        if (!ClientRegistry.QUIVER_KEYBIND.isUnbound() && mc.player instanceof IQuiverPlayer qe) {
            boolean keyDown = ClientRegistry.QUIVER_KEYBIND.isDown();
            if (keyDown) SelectableContainerItemHud.getInstance().setUsingKeybind(
                    qe.supplementaries$getQuiverSlot(), mc.player);
        }
    }
}
