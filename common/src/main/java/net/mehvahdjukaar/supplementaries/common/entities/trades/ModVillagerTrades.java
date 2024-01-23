package net.mehvahdjukaar.supplementaries.common.entities.trades;

import net.mehvahdjukaar.moonlight.api.trades.ItemListingRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.world.entity.npc.VillagerTrades;

public class ModVillagerTrades {

    public static VillagerTrades.ItemListing[] getRedMerchantTrades() {
        return ItemListingRegistry.getSpecialListings(ModEntities.RED_MERCHANT.get(),
                1).toArray(VillagerTrades.ItemListing[]::new);
    }

    //runs on init since we need to be early enough to register stuff to forge busses
    public static void init() {

        ItemListingRegistry.registerSerializer(Supplementaries.res("random_firework_star"), StarItemListing.CODEC);
        ItemListingRegistry.registerSerializer(Supplementaries.res("random_firework"), RocketItemListing.CODEC);
        ItemListingRegistry.registerSerializer(Supplementaries.res("wrap_on_christmas"), PresentItemListing.CODEC);
        ItemListingRegistry.registerSerializer(Supplementaries.res("structure_map"), StructureMapListing.CODEC);
        ItemListingRegistry.registerSerializer(Supplementaries.res("adventurer_map"), RandomAdventurerMapListing.CODEC);
    }
}
