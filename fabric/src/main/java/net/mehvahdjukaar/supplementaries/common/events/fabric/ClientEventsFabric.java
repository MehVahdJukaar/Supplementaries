package net.mehvahdjukaar.supplementaries.common.events.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeHud;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.client.hud.SlimedOverlayHud;
import net.mehvahdjukaar.supplementaries.client.hud.fabric.SelectableContainerItemHudImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.PartyHatLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.SlimedLayer;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ClientEventsFabric {

    public static void init() {
        ClientEntityEvents.ENTITY_LOAD.register(ClientEvents::onEntityLoad);

        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
        ScreenEvents.AFTER_INIT.register((m, s, x, y) -> {
            if (CompatHandler.CLOTH_CONFIG || CompatHandler.YACL) {
                List<? extends GuiEventListener> listeners = s.children();
                ClientEvents.addConfigButton(s, listeners, e -> {
                    List<GuiEventListener> c = (List<GuiEventListener>) s.children();
                    c.add(e);
                });
            }
        });

        ClientPreAttackCallback.EVENT.register((minecraft, localPlayer, i) -> {
            if (CannonController.isActive()) {
                CannonController.onPlayerAttack();
                return true;
            }
            return false;
        });


        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);


        HudRenderCallback.EVENT.register(ClientEventsFabric::onRenderHud);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((t, r, e, c) -> {
            if (r instanceof PlayerRenderer) {
                e.register(new QuiverLayer(r, false));

            } else if (t == EntityType.SKELETON) {
                e.register(new QuiverLayer(r, true));
            } else if (t == EntityType.STRAY) {
                e.register(new QuiverLayer(r, true));
            } else if (t == EntityType.CREEPER) {
                e.register(new PartyHatLayer.Creeper(r, c.getModelSet(), c.getItemInHandRenderer()));
            } else if (t == EntityType.CREEPER) {
                e.register(new PartyHatLayer.Creeper(r, c.getModelSet(), c.getItemInHandRenderer()));
            }
            e.register(new SlimedLayer<>(r));
        });

    }

    private static void onRenderHud(GuiGraphics graphics, DeltaTracker partialTicks) {
        SelectableContainerItemHudImpl.INSTANCE.render(graphics, partialTicks);
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
