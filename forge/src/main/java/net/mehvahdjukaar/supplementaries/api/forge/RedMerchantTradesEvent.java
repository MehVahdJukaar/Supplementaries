package net.mehvahdjukaar.supplementaries.api.forge;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class RedMerchantTradesEvent extends Event
{

    protected List<VillagerTrades.ItemListing> trades;

    public RedMerchantTradesEvent(List<VillagerTrades.ItemListing> generic)
    {
        this.trades = generic;
    }

    public List<VillagerTrades.ItemListing> getTrades()
    {
        return trades;
    }
}

