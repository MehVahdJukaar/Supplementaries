package net.mehvahdjukaar.supplementaries.common;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.mehvahdjukaar.selene.map.MapDecorationHandler;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class AdventurerMapsHandler {

    private static final int SEARCH_RADIUS = 100;
    private static final List<TradeData> customTrades = new ArrayList<>();

    private static final Map<StructureFeature<?>, Pair<CustomDecorationType<?, ?>, Integer>> defaultStructureMarkers = new HashMap<>();

    private static final List<StructureFeature<?>> randomMapPool = Arrays.asList(StructureFeature.SHIPWRECK, StructureFeature.RUINED_PORTAL, StructureFeature.SWAMP_HUT,
            StructureFeature.BASTION_REMNANT, StructureFeature.JUNGLE_TEMPLE, StructureFeature.DESERT_PYRAMID, StructureFeature.PILLAGER_OUTPOST, StructureFeature.MINESHAFT,
            StructureFeature.OCEAN_RUIN, StructureFeature.IGLOO, StructureFeature.END_CITY);

    static {
        defaultStructureMarkers.put(StructureFeature.SHIPWRECK, Pair.of(CMDreg.SHIPWRECK_TYPE, 0x34200f));
        defaultStructureMarkers.put(StructureFeature.IGLOO, Pair.of(CMDreg.IGLOO_TYPE, 0x99bdc2));
        defaultStructureMarkers.put(StructureFeature.RUINED_PORTAL, Pair.of(CMDreg.RUINED_PORTAL_TYPE, 0x5f30b5));
        defaultStructureMarkers.put(StructureFeature.VILLAGE, Pair.of(CMDreg.VILLAGE_TYPE, 0xba8755));
        defaultStructureMarkers.put(StructureFeature.OCEAN_RUIN, Pair.of(CMDreg.OCEAN_RUIN_TYPE, 0x3a694d));
        defaultStructureMarkers.put(StructureFeature.PILLAGER_OUTPOST, Pair.of(CMDreg.PILLAGER_OUTPOST_TYPE, 0x1f1100));
        defaultStructureMarkers.put(StructureFeature.DESERT_PYRAMID, Pair.of(CMDreg.DESERT_PYRAMID_TYPE, 0x806d3f));
        defaultStructureMarkers.put(StructureFeature.JUNGLE_TEMPLE, Pair.of(CMDreg.JUNGLE_TEMPLE_TYPE, 0x526638));
        defaultStructureMarkers.put(StructureFeature.BASTION_REMNANT, Pair.of(CMDreg.BASTION_TYPE, 0x2c292f));
        defaultStructureMarkers.put(StructureFeature.END_CITY, Pair.of(CMDreg.END_CITY_TYPE, 0x9c73ab));
        defaultStructureMarkers.put(StructureFeature.SWAMP_HUT, Pair.of(CMDreg.SWAMP_HUT_TYPE, 0x1b411f));
        defaultStructureMarkers.put(StructureFeature.NETHER_BRIDGE, Pair.of(CMDreg.NETHER_FORTRESS, 0x3c080b));
        defaultStructureMarkers.put(StructureFeature.MINESHAFT, Pair.of(CMDreg.MINESHAFT_TYPE, 0x808080));

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
        CustomDecorationType<?, ?> type = defaultStructureMarkers.get(structure).getLeft();
        if (type == null) type = MapDecorationHandler.GENERIC_STRUCTURE_TYPE;
        return type;
    }

    private static int getVanillaColor(StructureFeature<?> structure) {
        if (defaultStructureMarkers.containsKey(structure))
            return defaultStructureMarkers.get(structure).getRight();
        return -1;
    }


    public static void loadCustomTrades() {
        //only called once when server starts
        if (!customTrades.isEmpty()) return;

        try {
            List<? extends List<String>> tradeData = ConfigHandler.safeGetListString(ServerConfigs.SERVER_SPEC, ServerConfigs.tweaks.CUSTOM_ADVENTURER_MAPS_TRADES);
            ;
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
                            Supplementaries.LOGGER.warn("skipping configs 'custom_adventurer_maps' (" + l.toString() + "): invalid level, must be between 1 and 5");
                            continue;
                        }
                        if (s > 2) minPrice = Integer.parseInt(l.get(2));
                        if (s > 3) maxPrice = Integer.parseInt(l.get(3));
                        if (s > 4) mapName = l.get(4);
                        if (s > 5) mapColor = Integer.parseInt(l.get(5).replace("0x", ""), 16);
                        if (s > 6) marker = new ResourceLocation(l.get(6));


                        customTrades.add(new TradeData(structure, level, minPrice, maxPrice, mapName, mapColor, marker));
                    } catch (Exception e) {
                        Supplementaries.LOGGER.warn("wrong formatting for configs 'custom_adventurer_maps'(" + l.toString() + "), skipping it :" + e);
                    }
                }

            }
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to parse config 'custom_adventurer_maps', skipping them.");
        }
    }


    private static class TradeData {
        public final ResourceLocation structure;

        //optional
        public final int level;
        public final int minPrice;
        public final int maxPrice;

        public final String mapName;
        public final int mapColor;
        public final ResourceLocation marker;

        private TradeData(ResourceLocation structure, int level, int minPrice, int maxPrice,
                          @Nullable String name, int mapColor, @Nullable ResourceLocation marker) {
            this.structure = structure;
            this.marker = marker;
            this.mapName = name;
            this.mapColor = mapColor;
            this.level = level;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }


    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            for (TradeData data : customTrades) {
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
            if (!(world instanceof ServerLevel)) {
                return ItemStack.EMPTY;
            } else {
                ServerLevel serverWorld = ((ServerLevel) world);
                if (!serverWorld.getServer().getWorldData().worldGenSettings().generateFeatures())
                    return ItemStack.EMPTY;

                List<StructureFeature<?>> pool = randomMapPool.stream().filter(s -> serverWorld.getChunkSource().getGenerator()
                        .getBiomeSource().canGenerateStructure(s)).collect(Collectors.toList());

                int size = pool.size();
                if (size > 0) {
                    StructureFeature<?> structure = pool.get(serverWorld.random.nextInt(size));
                    BlockPos toPos = ((ServerLevel) world).findNearestMapFeature(structure, pos, SEARCH_RADIUS, true);
                    if (toPos != null) {
                        ItemStack stack = MapItem.create(world, toPos.getX(), toPos.getZ(), (byte) 2, true, true);
                        MapItem.renderBiomePreviewMap((ServerLevel) world, stack);

                        //adds custom decoration
                        MapDecorationHandler.addTargetDecoration(stack, toPos, getVanillaMarker(structure), 0x78151a);
                        stack.setHoverName(new TranslatableComponent("filled_map.adventure"));
                        return stack;
                    }
                }
                return ItemStack.EMPTY;
            }
        }
    }

    private static class AdventureMapTrade implements VillagerTrades.ItemListing {
        public final TradeData tradeData;

        private AdventureMapTrade(TradeData data) {
            this.tradeData = data;
        }

        @Override
        public MerchantOffer getOffer(@Nonnull Entity entity, @Nonnull Random random) {

            int i = random.nextInt(Math.max(1, tradeData.maxPrice - tradeData.minPrice + 1) + tradeData.minPrice);

            ItemStack itemstack = createMap(entity.level, entity.blockPosition());
            if (itemstack.isEmpty()) return null;

            return new MerchantOffer(new ItemStack(Items.EMERALD, i), new ItemStack(Items.COMPASS), itemstack, 12, Math.max(1, 5 * (tradeData.level - 1)), 0.2F);
        }

        private ItemStack createMap(Level world, BlockPos pos) {
            StructureFeature<?> structure = ForgeRegistries.STRUCTURE_FEATURES.getValue(tradeData.structure);


            if (!(world instanceof ServerLevel) || structure == null) {
                return ItemStack.EMPTY;
            } else {
                BlockPos toPos = ((ServerLevel) world).findNearestMapFeature(structure, pos, SEARCH_RADIUS, true);
                if (toPos == null) {
                    return ItemStack.EMPTY;
                } else {
                    ItemStack stack = MapItem.create(world, toPos.getX(), toPos.getZ(), (byte) 2, true, true);
                    MapItem.renderBiomePreviewMap((ServerLevel) world, stack);


                    //vanilla maps for backwards compat
                    if (structure instanceof OceanMonumentFeature) {
                        MapItemSavedData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MONUMENT);
                    } else if (structure instanceof WoodlandMansionFeature) {
                        MapItemSavedData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MANSION);
                    } else {
                        //adds custom decoration
                        int color = tradeData.mapColor == 0xffffff ? getVanillaColor(structure) : tradeData.mapColor;
                        if (tradeData.marker == null) {
                            MapDecorationHandler.addTargetDecoration(stack, toPos, getVanillaMarker(structure), color);
                        } else {
                            MapDecorationHandler.addTargetDecoration(stack, toPos, tradeData.marker, color);
                        }

                    }

                    Component name = new TranslatableComponent(tradeData.mapName == null ?
                            "filled_map." + structure.getFeatureName().toLowerCase(Locale.ROOT) : tradeData.mapName);
                    stack.setHoverName(name);
                    return stack;
                }
            }
        }
    }

}
