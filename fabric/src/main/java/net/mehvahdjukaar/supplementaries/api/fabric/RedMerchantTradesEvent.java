package net.mehvahdjukaar.supplementaries.api.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.List;
import java.util.function.Consumer;

public class RedMerchantTradesEvent {

    /**
     * Register an event to modify Red Merchant trades
     */
    public static final Event<Consumer<List<VillagerTrades.ItemListing>>> MODIFY_TRADES = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (listings) -> {
        for (var event : listeners) {
            event.accept(listings);
        }
    });

}
