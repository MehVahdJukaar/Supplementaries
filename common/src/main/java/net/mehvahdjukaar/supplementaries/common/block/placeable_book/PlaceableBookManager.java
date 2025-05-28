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
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.overrides.SuppAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendBookDataPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
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

public class PlaceableBookManager extends SimpleJsonResourceReloadListener {

    private static final SidedInstance<PlaceableBookManager> INSTANCES = SidedInstance.of(PlaceableBookManager::new);

    private final MapRegistry<BookType> books = new MapRegistry<>("placeable_books");
    private final Multimap<Item, BookType> itemToBooks = HashMultimap.create();
    private final HolderLookup.Provider registryAccess;
    private final SuppAdditionalPlacement horizontalPlacement;
    private final SuppAdditionalPlacement verticalPlacement;

    public PlaceableBookManager(HolderLookup.Provider registryAccess) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "placeable_books");
        this.registryAccess = registryAccess;

        INSTANCES.set(registryAccess, this);

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

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        DynamicOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        Codec<Optional<BookType>> codec = ForgeHelper.conditionalCodec(BookType.CODEC);
        Map<ResourceLocation, BookType> bookTypes = new HashMap<>();
        for (var entry : object.entrySet()) {
            codec.parse(ops, entry.getValue())
                    .getOrThrow().ifPresent(type -> bookTypes.put(entry.getKey(), type));
        }
        this.setData(bookTypes);
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

    public static void sendDataToClient(ServerPlayer player) {
        PlaceableBookManager instance = INSTANCES.get(player.level().registryAccess());
        NetworkHelper.sendToClientPlayer(player, new ClientBoundSendBookDataPacket(instance.books.getEntries()));
    }

}
