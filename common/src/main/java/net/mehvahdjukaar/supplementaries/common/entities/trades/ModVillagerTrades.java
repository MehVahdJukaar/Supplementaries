package net.mehvahdjukaar.supplementaries.common.entities.trades;

import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.npc.VillagerTrades;

public class ModVillagerTrades {

    public static VillagerTrades.ItemListing[] getRedMerchantTrades(HolderLookup.Provider reg) {
        return ItemListingManager.getSpecialListings(ModEntities.RED_MERCHANT.get(),
                1, reg).toArray(VillagerTrades.ItemListing[]::new);
    }

    //runs on init since we need to be early enough to register stuff to forge busses
    public static void init() {
        ItemListingManager.registerSerializer(Supplementaries.res("random_firework_star"), StarItemListing.CODEC);
        ItemListingManager.registerSerializer(Supplementaries.res("random_firework"), RocketItemListing.CODEC);
        ItemListingManager.registerSerializer(Supplementaries.res("wrap_on_christmas"), PresentItemListing.CODEC);
        ItemListingManager.registerSerializer(Supplementaries.res("structure_map"), StructureMapListing.CODEC);
        ItemListingManager.registerSerializer(Supplementaries.res("adventurer_map"), RandomAdventurerMapListing.CODEC);
    }
}
