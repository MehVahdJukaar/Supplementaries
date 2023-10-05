package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdventurerMapsHandler extends SimpleJsonResourceReloadListener {

    //cursed
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .registerTypeAdapter(AdventurerMapTrade.class, (JsonDeserializer<AdventurerMapTrade>)
                    (json, typeOfT, context) -> AdventurerMapTrade.CODEC.parse(JsonOps.INSTANCE, json)
                            .getOrThrow(false, e ->
                                    Supplementaries.LOGGER.error("failed to parse structure map trade: {}", e))
            ).create();

    public static final PreparableReloadListener RELOAD_INSTANCE = new AdventurerMapsHandler();

    public AdventurerMapsHandler() {
        super(GSON, "structure_maps");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {

        CUSTOM_MAPS_TRADES.clear();

        jsons.forEach((key, json) -> {
            //CUSTOM_MAPS_TRADES.add(GSON.fromJson(json, AdventurerMapTrade.class));
            var v = AdventurerMapTrade.CODEC.parse(JsonOps.INSTANCE, json);
            var data = v.getOrThrow(false, e -> Supplementaries.LOGGER.error("failed to parse structure map trade: {}", e));
            CUSTOM_MAPS_TRADES.add(data);
        });
        if (CUSTOM_MAPS_TRADES.size() != 0)
            Supplementaries.LOGGER.info("Loaded  " + CUSTOM_MAPS_TRADES.size() + " structure maps trades");
    }


    public static final int SEARCH_RADIUS = 150;
    private static final List<AdventurerMapTrade> CUSTOM_MAPS_TRADES = new ArrayList<>();

    public static void addTradesCallback() {

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 1, itemListings ->
                maybeAddCustomMap(itemListings, 1)
        );

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 2, itemListings -> {
            if (CommonConfigs.Tweaks.RANDOM_ADVENTURER_MAPS.get()) {
                itemListings.add(new RandomAdventureMapTrade());
            }
            maybeAddCustomMap(itemListings, 2);
        });

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 3, itemListings -> {
            maybeAddCustomMap(itemListings, 3);
        });

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 4, itemListings -> {
            maybeAddCustomMap(itemListings, 4);
        });

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 5, itemListings -> {
            maybeAddCustomMap(itemListings, 5);
        });
    }

    private static void maybeAddCustomMap(List<VillagerTrades.ItemListing> listings, int level) {
        for (var data : CUSTOM_MAPS_TRADES) {
            if (level == data.villagerLevel()) {
                listings.add(data);
            }
        }
    }

    private static class RandomAdventureMapTrade implements VillagerTrades.ItemListing {

        @Override
        public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
            int maxPrice = 11;
            int minPrice = 6;
            int price = random.nextInt(maxPrice - minPrice + 1) + minPrice;

            if (entity.level() instanceof ServerLevel serverLevel) {
                ItemStack itemstack = createMapOrQuill(serverLevel, entity.blockPosition(), null,
                        2, null, "filled_map.adventure", 0x78151a);

                int uses = CommonConfigs.Tweaks.QUILL_MAX_TRADES.get();
                int x = 6;
                int xp = (int) ((x * 12) / (float) uses);
                int cost = (int) (price * CommonConfigs.Tweaks.QUILL_TRADE_PRICE_MULT.get());

                return new MerchantOffer(new ItemStack(Items.EMERALD, cost), new ItemStack(Items.COMPASS), itemstack,
                        uses, xp, 0.2F);
            }
            return null;
        }
    }

    public static ItemStack createMapOrQuill(ServerLevel serverLevel, BlockPos pos, @Nullable HolderSet<Structure> targets,
                                             int zoom, @Nullable ResourceLocation mapMarker,
                                             @Nullable String name, int color) {
        if (CompatHandler.QUARK && CommonConfigs.Tweaks.QUARK_QUILL.get()) {
            var item = QuarkCompat.makeAdventurerQuill(serverLevel, targets,
                    SEARCH_RADIUS, true, zoom, null, name, color);
            item.setHoverName(Component.translatable(name));
            return item;
        }

        if (!serverLevel.getServer().getWorldData().worldGenOptions().generateStructures())
            return ItemStack.EMPTY;

        if (targets == null) {
            targets = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(ModTags.ADVENTURE_MAP_DESTINATIONS)
                    .orElse(null);
            if (targets == null) targets = HolderSet.direct();
        }

        var found = StructureLocator.findNearestRandomMapFeature(
                serverLevel, targets, pos, SEARCH_RADIUS, true);

        if (found != null) {
            BlockPos toPos = found.getFirst();
            return createStructureMap(serverLevel, toPos, found.getSecond(), zoom, mapMarker, name, color);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    public static ItemStack createStructureMap(ServerLevel level, BlockPos pos, Holder<Structure> structure, int zoom,
                                               @Nullable ResourceLocation decoration, @Nullable String name,
                                               int color) {
        ItemStack stack = MapItem.create(level, pos.getX(), pos.getZ(), (byte) zoom, true, true);
        MapItem.renderBiomePreviewMap(level, stack);

        //adds custom decoration
        if (decoration == null) {
            var type = MapDataRegistry.getAssociatedType(structure);
            decoration = Utils.getID(type);
            if (color == 0) {
                color = type.getDefaultMapColor();
            }
        }
        MapHelper.addDecorationToMap(stack, pos, decoration, color);

        if (name != null) {
            stack.setHoverName(Component.translatable(name));
        }
        return stack;
    }


    public static ItemStack createCustomMapForTrade(Level level, BlockPos pos, ResourceLocation structureName,
                                                    @Nullable String mapName, int mapColor, @Nullable ResourceLocation mapMarker) {

        if (level instanceof ServerLevel serverLevel) {
            var destination = TagKey.create(Registries.STRUCTURE, structureName);
            String name = mapName == null ?
                    "filled_map." + structureName.getPath().toLowerCase(Locale.ROOT) : mapName;
            var targets = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .getTag(destination).orElse(null);

            if (targets != null) {
                return createMapOrQuill(serverLevel, pos, targets, 2, mapMarker, name, mapColor);
            }
        }
        return ItemStack.EMPTY;
    }


}