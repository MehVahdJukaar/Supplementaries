package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.overrides.SuppAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendBookDataPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlaceableBookManager  {
    public static final ResourceKey<Registry<BookType>> REGISTRY_KEY = ResourceKey
            .createRegistryKey(Supplementaries.res("placeable_books"));

    private static final MapRegistry<BookType> books = new MapRegistry<>("placeable_books");
    private static final SidedInstance<Multimap<Item, BookType>> itemToBooks = SidedInstance.of(
            r -> HashMultimap.create()); //populate here
    private final SuppAdditionalPlacement horizontalPlacement;
    private final SuppAdditionalPlacement verticalPlacement;


    public static void init(){
        RegHelper.registerDatapackRegistry(REGISTRY_KEY, BookType.CODEC, BookType.CODEC);

    }
    public PlaceableBookManager(HolderLookup.Provider registryAccess) {
        this.horizontalPlacement = new SuppAdditionalPlacement(ModRegistry.BOOK_PILE_H.get());
        this.verticalPlacement = new SuppAdditionalPlacement(ModRegistry.BOOK_PILE.get());
    }

    public static PlaceableBookManager getInstance(HolderLookup.Provider ra) {
        return INSTANCES.get(ra);
    }

    public static PlaceableBookManager getInstance(@NotNull Level level) {
        Preconditions.checkNotNull(level);
        return getInstance(level.registryAccess());
    }


    public void setData(Map<ResourceLocation, BookType> bookTypes) {
        //this.books.clear();
        //clear previous placements
        for (var entry : itemToBooks.entries()) {
            AdditionalItemPlacementsAPI.unregisterPlacement(entry.getKey());
        }
        this.itemToBooks.clear();
        for (var entry : bookTypes.entrySet()) {
            BookType value = entry.getValue();
            this.books.register(entry.getKey(), value);
            Item item = value.item();
            if (itemToBooks.containsKey(item)) {
                Supplementaries.LOGGER.warn("Duplicate book type for item: {}, overriding", item);
            }
            this.itemToBooks.put(item, value);
            AdditionalItemPlacementsAPI.registerPlacement(item, value.isHorizontal() ? horizontalPlacement : verticalPlacement);
        }
    }

    @Nullable
    public BookType get(Item item, boolean horizontal) {
        for (var entry : itemToBooks.get(item)) {
            if (entry.isHorizontal() == horizontal || CommonConfigs.Tweaks.MIXED_BOOKS.get()) {
                return entry;
            }
        }
        return null;
    }

}
