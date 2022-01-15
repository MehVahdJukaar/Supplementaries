package net.mehvahdjukaar.supplementaries.common.entities.trades;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.mehvahdjukaar.selene.map.MapDecorationHandler;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AdventurerMapsHandler {

    private static final int SEARCH_RADIUS = 100;
    private static final List<TradeData> CUSTOM_MAPS_TRADES = new ArrayList<>();

    private static final Map<StructureFeature<?>, Pair<CustomDecorationType<?, ?>, Integer>> DEFAULT_STRUCTURE_MARKERS = new HashMap<>();

    private static final List<StructureFeature<?>> RANDOM_MAP_POOL = Arrays.asList(StructureFeature.SHIPWRECK, StructureFeature.RUINED_PORTAL, StructureFeature.SWAMP_HUT,
            StructureFeature.BASTION_REMNANT, StructureFeature.JUNGLE_TEMPLE, StructureFeature.DESERT_PYRAMID, StructureFeature.PILLAGER_OUTPOST, StructureFeature.MINESHAFT,
            StructureFeature.OCEAN_RUIN, StructureFeature.IGLOO, StructureFeature.END_CITY);

    static {
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.SHIPWRECK, Pair.of(CMDreg.SHIPWRECK_TYPE, 0x34200f));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.IGLOO, Pair.of(CMDreg.IGLOO_TYPE, 0x99bdc2));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.RUINED_PORTAL, Pair.of(CMDreg.RUINED_PORTAL_TYPE, 0x5f30b5));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.VILLAGE, Pair.of(CMDreg.VILLAGE_TYPE, 0xba8755));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.OCEAN_RUIN, Pair.of(CMDreg.OCEAN_RUIN_TYPE, 0x3a694d));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.PILLAGER_OUTPOST, Pair.of(CMDreg.PILLAGER_OUTPOST_TYPE, 0x1f1100));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.DESERT_PYRAMID, Pair.of(CMDreg.DESERT_PYRAMID_TYPE, 0x806d3f));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.JUNGLE_TEMPLE, Pair.of(CMDreg.JUNGLE_TEMPLE_TYPE, 0x526638));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.BASTION_REMNANT, Pair.of(CMDreg.BASTION_TYPE, 0x2c292f));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.END_CITY, Pair.of(CMDreg.END_CITY_TYPE, 0x9c73ab));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.SWAMP_HUT, Pair.of(CMDreg.SWAMP_HUT_TYPE, 0x1b411f));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.NETHER_BRIDGE, Pair.of(CMDreg.NETHER_FORTRESS, 0x3c080b));
        DEFAULT_STRUCTURE_MARKERS.put(StructureFeature.MINESHAFT, Pair.of(CMDreg.MINESHAFT_TYPE, 0x808080));

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

    private static CustomDecorationType<?, ?> getVanillaMarker(StructureFeature<?> structure) {
        CustomDecorationType<?, ?> type = DEFAULT_STRUCTURE_MARKERS.get(structure).getLeft();
        if (type == null) type = MapDecorationHandler.GENERIC_STRUCTURE_TYPE;
        return type;
    }

    private static int getVanillaColor(StructureFeature<?> structure) {
        if (DEFAULT_STRUCTURE_MARKERS.containsKey(structure))
            return DEFAULT_STRUCTURE_MARKERS.get(structure).getRight();
        return -1;
    }


    public static void loadCustomTrades() {
        //only called once when server starts
        if (!CUSTOM_MAPS_TRADES.isEmpty()) return;

        try {
            List<? extends List<String>> tradeData = ConfigHandler.safeGetListString(ServerConfigs.SERVER_SPEC, ServerConfigs.tweaks.CUSTOM_ADVENTURER_MAPS_TRADES);

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
                      @Nullable String mapName, int mapColor, @Nullable ResourceLocation marker) {}


    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            for (TradeData data : CUSTOM_MAPS_TRADES) {
                if (data != null)
                    try {
                        trades.get(data.level).add(new AdventureMapTrade(data));
                    } catch (Exception e) {
                        Supplementaries.LOGGER.warn("failed to load custom adventurer map trade map for structure " + data.structure.toString());
                    }

            }
            if (ServerConfigs.tweaks.RANDOM_ADVENTURER_MAPS.get()) {
                trades.get(2).add(new RandomAdventureMapTrade());
            }
        }
    }


    private static class RandomAdventureMapTrade implements VillagerTrades.ItemListing {

        private RandomAdventureMapTrade() {
        }

        @Override
        public MerchantOffer getOffer(@Nonnull Entity entity, @Nonnull Random random) {
            int maxPrice = 13;
            int minPrice = 7;
            int level = 2;
            int i = random.nextInt(maxPrice - minPrice + 1) + minPrice;

            ItemStack itemstack = createMap(entity.level, entity.blockPosition());
            if (itemstack.isEmpty()) return null;

            return new MerchantOffer(new ItemStack(Items.EMERALD, i), new ItemStack(Items.COMPASS), itemstack, 12, 5, 0.2F);
        }

        private ItemStack createMap(Level world, BlockPos pos) {
            if (world instanceof ServerLevel serverWorld) {
                if (!serverWorld.getServer().getWorldData().worldGenSettings().generateFeatures())
                    return ItemStack.EMPTY;

                //TODO: readd
                //List<StructureFeature<?>> pool = RANDOM_MAP_POOL.stream().filter(s -> serverWorld.getChunkSource().getGenerator()
                //        .getBiomeSource().canGenerateStructure(s)).collect(Collectors.toList());

                List<StructureFeature<?>> pool = RANDOM_MAP_POOL;

                int size = pool.size();
                if (size > 0) {
                    StructureFeature<?> structure = pool.get(serverWorld.random.nextInt(size));
                    BlockPos toPos = serverWorld.findNearestMapFeature(structure, pos, SEARCH_RADIUS, true);
                    if (toPos != null) {
                        ItemStack stack = MapItem.create(world, toPos.getX(), toPos.getZ(), (byte) 2, true, true);
                        MapItem.renderBiomePreviewMap(serverWorld, stack);

                        //adds custom decoration
                        MapDecorationHandler.addTargetDecoration(stack, toPos, getVanillaMarker(structure), 0x78151a);
                        stack.setHoverName(new TranslatableComponent("filled_map.adventure"));
                        return stack;
                    }
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
        public MerchantOffer getOffer(@Nonnull Entity entity, @Nonnull Random random) {

            int i = Math.max(1,random.nextInt(Math.max(1, tradeData.maxPrice - tradeData.minPrice)) + tradeData.minPrice);

            ItemStack itemstack = createStructureMap(entity.level, entity.blockPosition(),
                    tradeData.structure, tradeData.mapName, tradeData.mapColor, tradeData.marker);
            if (itemstack.isEmpty()) return null;

            return new MerchantOffer(new ItemStack(Items.EMERALD, i), new ItemStack(Items.COMPASS), itemstack, 12, Math.max(1, 5 * (tradeData.level - 1)), 0.2F);
        }


    }


    public static ItemStack createStructureMap(Level world, BlockPos pos, ResourceLocation structureName,
                                               @Nullable String mapName, int mapColor, @Nullable ResourceLocation mapMarker) {
        StructureFeature<?> structure = ForgeRegistries.STRUCTURE_FEATURES.getValue(structureName);

        if (world instanceof ServerLevel serverLevel && structure != null) {

            BlockPos toPos = serverLevel.findNearestMapFeature(structure, pos, SEARCH_RADIUS, true);
            if (toPos == null) {
                return ItemStack.EMPTY;
            } else {
                ItemStack stack = MapItem.create(world, toPos.getX(), toPos.getZ(), (byte) 2, true, true);
                MapItem.renderBiomePreviewMap(serverLevel, stack);


                //vanilla maps for backwards compat
                if (structure instanceof OceanMonumentFeature) {
                    MapItemSavedData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MONUMENT);
                } else if (structure instanceof WoodlandMansionFeature) {
                    MapItemSavedData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MANSION);
                } else {
                    //adds custom decoration
                    int color = mapColor == 0xffffff ? getVanillaColor(structure) : mapColor;
                    if (mapMarker == null) {
                        MapDecorationHandler.addTargetDecoration(stack, toPos, getVanillaMarker(structure), color);
                    } else {
                        MapDecorationHandler.addTargetDecoration(stack, toPos, mapMarker, color);
                    }

                }

                Component name = new TranslatableComponent(mapName == null ?
                        "filled_map." + structure.getFeatureName().toLowerCase(Locale.ROOT) : mapName);
                stack.setHoverName(name);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }


}
