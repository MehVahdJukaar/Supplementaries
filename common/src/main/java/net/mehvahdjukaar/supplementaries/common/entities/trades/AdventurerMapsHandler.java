package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.mehvahdjukaar.supplementaries.reg.generation.structure.StructureLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.StructureTags;
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
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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


    private static final int SEARCH_RADIUS = 100;
    private static final List<AdventurerMapTrade> CUSTOM_MAPS_TRADES = new ArrayList<>();

    private static final Map<TagKey<Structure>,
            Pair<ResourceLocation, Integer>> DEFAULT_STRUCTURE_MARKERS = new HashMap<>();


    private static void associateStructureMarker(TagKey<Structure> tag, ResourceLocation res, int color) {
        DEFAULT_STRUCTURE_MARKERS.put(tag, Pair.of(res, color));
    }

    static {
        associateStructureMarker(StructureTags.SHIPWRECK, CMDreg.SHIPWRECK_TYPE, 0x34200f);
        associateStructureMarker(ModTags.IGLOO, CMDreg.IGLOO_TYPE, 0x99bdc2);
        associateStructureMarker(StructureTags.RUINED_PORTAL, CMDreg.RUINED_PORTAL_TYPE, 0x5f30b5);
        associateStructureMarker(StructureTags.VILLAGE, CMDreg.VILLAGE_TYPE, 0xba8755);
        associateStructureMarker(StructureTags.OCEAN_RUIN, CMDreg.OCEAN_RUIN_TYPE, 0x3a694d);
        associateStructureMarker(ModTags.PILLAGER_OUTPOST, CMDreg.PILLAGER_OUTPOST_TYPE, 0x1f1100);
        associateStructureMarker(ModTags.DESERT_PYRAMID, CMDreg.DESERT_PYRAMID_TYPE, 0x806d3f);
        associateStructureMarker(ModTags.JUNGLE_TEMPLE, CMDreg.JUNGLE_TEMPLE_TYPE, 0x526638);
        associateStructureMarker(ModTags.BASTION_REMNANT, CMDreg.BASTION_TYPE, 0x2c292f);
        associateStructureMarker(ModTags.END_CITY, CMDreg.END_CITY_TYPE, 0x9c73ab);
        associateStructureMarker(ModTags.SWAMP_HUT, CMDreg.SWAMP_HUT_TYPE, 0x1b411f);
        associateStructureMarker(ModTags.NETHER_FORTRESS, CMDreg.NETHER_FORTRESS, 0x3c080b);
        associateStructureMarker(StructureTags.MINESHAFT, CMDreg.MINESHAFT_TYPE, 0x808080);
    }

    private static Pair<MapDecorationType<?, ?>, Integer> getStructureMarker(Holder<Structure> structure) {
        ResourceLocation res = new ResourceLocation("");
        int color = -1;
        for (var v : DEFAULT_STRUCTURE_MARKERS.entrySet()) {
            if (structure.is(v.getKey())) {
                res = v.getValue().getFirst();
                color = v.getValue().getSecond();
            }
        }
        return Pair.of(MapDecorationRegistry.get(res), color);
    }

    private static Pair<MapDecorationType<?, ?>, Integer> getStructureMarker(TagKey<Structure> tag) {
        var g = DEFAULT_STRUCTURE_MARKERS.getOrDefault(tag, Pair.of(new ResourceLocation("selene:generic_structure"), -1));
        return Pair.of(MapDecorationRegistry.get(g.getFirst()), g.getSecond());
    }

    public static void addTradesCallback() {

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 1, itemListings -> {
            maybeAddCustomMap(itemListings, 1);
        });

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

    private static void maybeAddCustomMap(List<VillagerTrades.ItemListing> listings, int level){
        for (var data : CUSTOM_MAPS_TRADES) {
            if(level == data.villagerLevel()){
                listings.add(data);
            }
        }
    }

    private static class RandomAdventureMapTrade implements VillagerTrades.ItemListing {

        @Override
        public MerchantOffer getOffer(@Nonnull Entity entity, @Nonnull RandomSource random) {
            int maxPrice = 13;
            int minPrice = 7;
            int level = 2;
            int i = random.nextInt(maxPrice - minPrice + 1) + minPrice;

            ItemStack itemstack = createMap(entity.level, entity.blockPosition());
            if (itemstack.isEmpty()) return null;

            return new MerchantOffer(new ItemStack(Items.EMERALD, i), new ItemStack(Items.COMPASS), itemstack, 12, 5, 0.2F);
        }

        private ItemStack createMap(Level level, BlockPos pos) {
            if (level instanceof ServerLevel serverLevel) {
                if (!serverLevel.getServer().getWorldData().worldGenSettings().generateStructures())
                    return ItemStack.EMPTY;

                var found = StructureLocator.findNearestRandomMapFeature(
                        serverLevel, ModTags.ADVENTURE_MAP_DESTINATIONS,
                        pos, 250, true);
                if (found != null) {
                    BlockPos toPos = found.getFirst();
                    ItemStack stack = MapItem.create(level, toPos.getX(), toPos.getZ(), (byte) 2, true, true);
                    MapItem.renderBiomePreviewMap(serverLevel, stack);

                    var decoration = getStructureMarker(found.getSecond());

                    //adds custom decoration
                    MapHelper.addDecorationToMap(stack, toPos, decoration.getFirst(), 0x78151a);
                    stack.setHoverName(Component.translatable("filled_map.adventure"));
                    return stack;
                }

            }
            return ItemStack.EMPTY;
        }
    }


    public static ItemStack createStructureMap(Level world, BlockPos pos, ResourceLocation structureName,
                                               @Nullable String mapName, int mapColor, @Nullable ResourceLocation mapMarker) {

        var destination = TagKey.create(Registry.STRUCTURE_REGISTRY, structureName);

        if (world instanceof ServerLevel serverLevel) {

            BlockPos toPos = serverLevel.findNearestMapStructure(destination, pos, SEARCH_RADIUS, true);
            if (toPos == null) {
                return ItemStack.EMPTY;
            } else {
                ItemStack stack = MapItem.create(world, toPos.getX(), toPos.getZ(), (byte) 2, true, true);
                MapItem.renderBiomePreviewMap(serverLevel, stack);


                //vanilla maps for backwards compat
                if (structureName.getPath().equals("ocean_monument")) {
                    MapItemSavedData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MONUMENT);
                } else if (structureName.getPath().equals("woodland_mansion")) {
                    MapItemSavedData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MANSION);
                } else {
                    //adds custom decoration

                    var decoration = getStructureMarker(destination);

                    int color = mapColor == 0xffffff ? decoration.getSecond() : mapColor;
                    if (mapMarker == null) {
                        MapHelper.addDecorationToMap(stack, toPos, decoration.getFirst(), color);
                    } else {
                        MapHelper.addDecorationToMap(stack, toPos, mapMarker, color);
                    }

                }

                Component name = Component.translatable(mapName == null ?
                        "filled_map." + structureName.getPath().toLowerCase(Locale.ROOT) : mapName);
                stack.setHoverName(name);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }


}
