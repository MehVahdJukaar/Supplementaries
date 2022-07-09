package net.mehvahdjukaar.supplementaries.common.events.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

public class ClientEventsFabric {

    public static void init() {
        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
        ScreenEvents.AFTER_INIT.register((m, s, x, y) ->{
            List<? extends GuiEventListener> listeners = s.children();
            ClientEvents.onScreenInit(s, listeners,e-> listeners.add(e));
        });
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);

    }

    public static void aa() {
    }
}
