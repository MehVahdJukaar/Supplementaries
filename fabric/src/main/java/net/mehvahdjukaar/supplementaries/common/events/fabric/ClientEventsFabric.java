package net.mehvahdjukaar.supplementaries.common.events.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.supplementaries.SupplementariesClient;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.QuiverArrowSelectGuiImpl;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
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

        WorldRenderEvents.START.register((c) -> SupplementariesClient.onRenderTick(c.tickDelta()));

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


    }

    private static void onRenderHud(PoseStack poseStack, float partialTicks) {
        QuiverArrowSelectGuiImpl.INSTANCE.render(poseStack, partialTicks);
    }
}
