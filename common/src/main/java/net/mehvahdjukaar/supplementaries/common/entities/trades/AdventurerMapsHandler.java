package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.moonlight.api.map.type.IMapDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.registry.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.reg.generation.structure.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
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

public class AdventurerMapsHandler {

    private static final int SEARCH_RADIUS = 100;
    private static final List<TradeData> CUSTOM_MAPS_TRADES = new ArrayList<>();

    private static final Map<TagKey<Structure>,
            Pair<ResourceLocation, Integer>> DEFAULT_STRUCTURE_MARKERS = new HashMap<>();


    private static void addStructureDecoration(TagKey<Structure> tag, ResourceLocation res, int color) {
        DEFAULT_STRUCTURE_MARKERS.put(tag, Pair.of(res, color));
    }

    private static void addStructureDecoration(TagKey<Structure> tag, IMapDecorationType<?, ?> type, int color) {
        addStructureDecoration(tag, type.getId(), color);
    }

    static {
        //tags here
        addStructureDecoration(StructureTags.SHIPWRECK, CMDreg.SHIPWRECK_TYPE, 0x34200f);
        addStructureDecoration(ModTags.IGLOO, CMDreg.IGLOO_TYPE, 0x99bdc2);
        addStructureDecoration(StructureTags.RUINED_PORTAL, CMDreg.RUINED_PORTAL_TYPE, 0x5f30b5);
        addStructureDecoration(StructureTags.VILLAGE, CMDreg.VILLAGE_TYPE, 0xba8755);
        addStructureDecoration(StructureTags.OCEAN_RUIN, CMDreg.OCEAN_RUIN_TYPE, 0x3a694d);
        addStructureDecoration(ModTags.PILLAGER_OUTPOST, CMDreg.PILLAGER_OUTPOST_TYPE, 0x1f1100);
        addStructureDecoration(ModTags.DESERT_PYRAMID, CMDreg.DESERT_PYRAMID_TYPE, 0x806d3f);
        addStructureDecoration(ModTags.JUNGLE_TEMPLE, CMDreg.JUNGLE_TEMPLE_TYPE, 0x526638);
        addStructureDecoration(ModTags.BASTION_REMNANT, CMDreg.BASTION_TYPE, 0x2c292f);
        addStructureDecoration(ModTags.END_CITY, CMDreg.END_CITY_TYPE, 0x9c73ab);
        addStructureDecoration(ModTags.SWAMP_HUT, CMDreg.SWAMP_HUT_TYPE, 0x1b411f);
        addStructureDecoration(ModTags.NETHER_FORTRESS, CMDreg.NETHER_FORTRESS, 0x3c080b);
        addStructureDecoration(StructureTags.MINESHAFT, CMDreg.MINESHAFT_TYPE, 0x808080);

        /*
        simpleMapTrade(Structure.SHIPWRECK);
        simpleMapTrade(Structure.IGLOO);
        simpleMapTrade(Structure.RUINED_PORTAL);
        simpleMapTrade(Structure.VILLAGE);
        simpleMapTrade(Structure.OCEAN_RUIN);
        simpleMapTrade(Structure.PILLAGER_OUTPOST);
        simpleMapTrade(Structure.DESERT_PYRAMID);
        simpleMapTrade(Structure.JUNGLE_TEMPLE);
        simpleMapTrade(Structure.BASTION_REMNANT);
        simpleMapTrade(Structure.END_CITY);
        simpleMapTrade(Structure.SWAMP_HUT);
        simpleMapTrade(Structure.NETHER_BRIDGE);
        simpleMapTrade(Structure.MINESHAFT);
        */

    }

    private static Pair<IMapDecorationType<?, ?>, Integer> getStructureMarker(Holder<Structure> structure) {
        ResourceLocation res = new ResourceLocation("selene:generic_structure");
        int color = -1;
        for (var v : DEFAULT_STRUCTURE_MARKERS.entrySet()) {
            if (structure.is(v.getKey())) {
                res = v.getValue().getFirst();
                color = v.getValue().getSecond();
            }
        }
        return Pair.of(MapDecorationRegistry.get(res), color);
    }

    private static Pair<IMapDecorationType<?, ?>, Integer> getStructureMarker(TagKey<Structure> tag) {
        var g = DEFAULT_STRUCTURE_MARKERS.getOrDefault(tag, Pair.of(new ResourceLocation("selene:generic_structure"), -1));
        return Pair.of(MapDecorationRegistry.get(g.getFirst()), g.getSecond());
    }


    public static void loadCustomTrades() {
        //only called once when server starts
        if (!CUSTOM_MAPS_TRADES.isEmpty()) return;
        try {
            List<? extends List<String>> tradeData = ServerConfigs.Tweaks.CUSTOM_ADVENTURER_MAPS_TRADES.get();

            for (List<String> l : tradeData) {
                int s = l.size();
                if (s > 0) {
                    try {

                        String res = l.get(0);
                        if (res.isEmpty()) continue;
                        ResourceLocation structure = new ResourceLocation(res);

                        //default values
                        int level = 2;
                        int minPrice = 7;
                        int maxPrice = 13;

                        String mapName = null;
                        int mapColor = 0xffffff;
                        ResourceLocation marker = null;


                        if (s > 1) level = Integer.parseInt(l.get(1));
                        if (level < 1 || level > 5) {
                            Supplementaries.LOGGER.warn("skipping configs 'custom_adventurer_maps' (" + l + "): invalid level, must be between 1 and 5");
                            continue;
                        }
                        if (s > 2) minPrice = Integer.parseInt(l.get(2));
                        if (s > 3) maxPrice = Integer.parseInt(l.get(3));
                        if (s > 4) mapName = l.get(4);
                        if (s > 5) mapColor = Integer.parseInt(l.get(5).replace("0x", ""), 16);
                        if (s > 6) marker = new ResourceLocation(l.get(6));


                        CUSTOM_MAPS_TRADES.add(new TradeData(structure, level, minPrice, maxPrice, mapName, mapColor, marker));
                    } catch (Exception e) {
                        Supplementaries.LOGGER.warn("wrong formatting for configs 'custom_adventurer_maps'(" + l + "), skipping it :" + e);
                    }
                }

            }
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to parse config 'custom_adventurer_maps', skipping them.");
        }
    }

    private record TradeData(ResourceLocation structure, int level, int minPrice, int maxPrice,
                             @Nullable String mapName, int mapColor, @Nullable ResourceLocation marker) {
    }

    public static void addTrades() {
        for (TradeData data : CUSTOM_MAPS_TRADES) {
            if (data != null)
                try {
                    RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, data.level, itemListings -> itemListings.add(new AdventureMapTrade(data)));
                } catch (Exception e) {
                    Supplementaries.LOGGER.warn("Failed to load custom adventurer map for structure " + data.structure.toString());
                }
        }
        if (ServerConfigs.Tweaks.RANDOM_ADVENTURER_MAPS.get()) {
            RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 2, itemListings -> itemListings.add(new RandomAdventureMapTrade()));
        }
    }


    private static class RandomAdventureMapTrade implements VillagerTrades.ItemListing {

        private RandomAdventureMapTrade() {
        }

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


    private static class AdventureMapTrade implements VillagerTrades.ItemListing {
        public final TradeData tradeData;

        private AdventureMapTrade(TradeData data) {
            this.tradeData = data;
        }

        @Override
        public MerchantOffer getOffer(@Nonnull Entity entity, @Nonnull RandomSource random) {

            int i = Math.max(1, random.nextInt(Math.max(1, tradeData.maxPrice - tradeData.minPrice)) + tradeData.minPrice);

            ItemStack itemstack = createStructureMap(entity.level, entity.blockPosition(),
                    tradeData.structure, tradeData.mapName, tradeData.mapColor, tradeData.marker);
            if (itemstack.isEmpty()) return null;

            return new MerchantOffer(new ItemStack(Items.EMERALD, i), new ItemStack(Items.COMPASS), itemstack, 12, Math.max(1, 5 * (tradeData.level - 1)), 0.2F);
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
