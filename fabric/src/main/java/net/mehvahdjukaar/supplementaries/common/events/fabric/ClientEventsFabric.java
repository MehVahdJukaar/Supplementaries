package net.mehvahdjukaar.supplementaries.common.events.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.QuiverArrowSelectGuiImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.items.AltimeterItemRenderer;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class ClientEventsFabric {

    public static void init() {
        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
        ScreenEvents.AFTER_INIT.register((m, s, x, y) -> {
            if (CompatHandler.CLOTH_CONFIG) {
                List<? extends GuiEventListener> listeners = s.children();
                ClientEvents.addConfigButton(s, listeners, e -> {
                    List<GuiEventListener> c = (List<GuiEventListener>) s.children();
                    c.add(e);
                });
            }
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
            }
        });

        //hack. good enough
        ClientLoginConnectionEvents.INIT.register((handler, client) -> AltimeterItemRenderer.onReload());

    }

    private static void onRenderHud(GuiGraphics graphics, float partialTicks) {
        QuiverArrowSelectGuiImpl.INSTANCE.render(graphics, partialTicks);
        //also using to check keybind

        if(ClientRegistry.QUIVER_KEYBIND.isUnbound()) {
            QuiverArrowSelectGui.setUsingKeybind(InputConstants.isKeyDown(
                    Minecraft.getInstance().getWindow().getWindow(),
                    ClientRegistry.QUIVER_KEYBIND.key.getValue()
            ));
        }
    }
}
