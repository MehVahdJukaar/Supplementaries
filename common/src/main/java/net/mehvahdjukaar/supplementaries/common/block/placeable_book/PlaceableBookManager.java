package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.overrides.SuppAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundFinalizeBookDataPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@ApiStatus.Internal
public class PlaceableBookManager {
    public static final ResourceKey<Registry<BookType>> REGISTRY_KEY = ResourceKey
            .createRegistryKey(Supplementaries.res("placeable_books"));

    private static final SidedInstance<Multimap<Item, BookType>> ITEMS_TO_BOOKS = SidedInstance.of(
            PlaceableBookManager::populateData); //populate here
    private static final Set<Item> ITEMS_WITH_PLACEMENTS = new HashSet<>();
    private static SuppAdditionalPlacement horizontalPlacement;
    private static SuppAdditionalPlacement verticalPlacement;


    public static void init() {
        RegHelper.registerDataPackRegistry(REGISTRY_KEY, BookType.CODEC, BookType.CODEC);
    }

    public static void setup() {
        horizontalPlacement = new SuppAdditionalPlacement(ModRegistry.BOOK_PILE_H.get());
        verticalPlacement = new SuppAdditionalPlacement(ModRegistry.BOOK_PILE.get());
    }

    public static HolderLookup.RegistryLookup<BookType> getRegistry(HolderLookup.Provider ra) {
        return ra.lookupOrThrow(REGISTRY_KEY);
    }

    //called on server start and on player first logn from datapack sync event
    public static void registerBookPlacements(RegistryAccess registryAccess) {
        //clear previous placements
        var reg = getRegistry(registryAccess);
        for (var entry : ITEMS_WITH_PLACEMENTS) {
            AdditionalItemPlacementsAPI.unregisterPlacement(entry);
        }

        for (var entry : reg.listElements().toList()) {
            BookType value = entry.value();
            Item item = value.item();
            AdditionalItemPlacementsAPI.registerPlacement(item,
                    value.isHorizontal() ? horizontalPlacement : verticalPlacement);
            ITEMS_WITH_PLACEMENTS.add(item);
        }
    }

    public static HashMultimap<Item, BookType> populateData(HolderLookup.Provider ra) {
        HashMultimap<Item, BookType> itemToBooks = HashMultimap.create();

        var reg = getRegistry(ra);
        for (var entry : reg.listElements().toList()) {
            BookType value = entry.value();
            Item item = value.item();
            itemToBooks.put(item, value);
        }
        return itemToBooks;
    }

    @Nullable
    public static BookType get(Item item, boolean horizontal, HolderLookup.Provider ra) {
        for (var entry : ITEMS_TO_BOOKS.get(ra).get(item)) {
            if (entry.isHorizontal() == horizontal || CommonConfigs.Tweaks.MIXED_BOOKS.get()) {
                return entry;
            }
        }
        return null;
    }

    public static void onDataSync(ServerPlayer player, boolean joined) {
        //just sends on login
        if (joined) {
            NetworkHelper.sendToClientPlayer(player, new ClientBoundFinalizeBookDataPacket());
        }
    }
}
