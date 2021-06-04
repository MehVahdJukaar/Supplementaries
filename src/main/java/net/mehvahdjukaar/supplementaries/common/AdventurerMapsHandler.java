package net.mehvahdjukaar.supplementaries.common;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapDecorationHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.OceanMonumentStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.WoodlandMansionStructure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdventurerMapsHandler {

    private static final int SEARCH_RADIUS = 100;
    private static final List<TradeData> customTrades = new ArrayList<>();

    public static void loadCustomTrades(){
        //only called once when server starts
        if(!customTrades.isEmpty())return;

        try {
            List<? extends List<String>> tradeData = ServerConfigs.tweaks.CUSTOM_ADVENTURER_MAPS_TRADES.get();
            for (List<String> l : tradeData) {
                int s = l.size();
                if (s == 9) {
                    try {
                        ResourceLocation structure = new ResourceLocation(l.get(0));
                        ResourceLocation marker = new ResourceLocation(l.get(1));
                        String mapName = l.get(2);
                        int color = Integer.parseInt(l.get(3).replace("0x", ""), 16);
                        int level = Integer.parseInt(l.get(4));
                        int minPrice = Integer.parseInt(l.get(5));
                        int maxPrice = Integer.parseInt(l.get(6));
                        int maxUses = Integer.parseInt(l.get(7));
                        int villagerXp = Integer.parseInt(l.get(8));

                        customTrades.add(new TradeData(structure,marker,mapName,color,level,minPrice,maxPrice,maxUses,villagerXp));
                    }
                    catch (Exception e){
                        Supplementaries.LOGGER.warn("failed to parse config 'custom_adventurer_maps' ("+l.toString()+"):"+e);
                    }
                }
                else{
                    Supplementaries.LOGGER.warn("failed to parse config 'custom_adventurer_maps' ("+l.toString()+"): expected 9 entries, found"+s);
                }
            }
        }
        catch (Exception e){
            Supplementaries.LOGGER.warn("failed to parse config 'custom_adventurer_maps'.");
        }
    }


    private static class TradeData {
        public final ResourceLocation structure;
        public final ResourceLocation marker;

        public final String mapName;
        public final int mapColor;

        public final int level;
        public final int minPrice;
        public final int maxPrice;
        public final int maxUses;
        public final int villagerXp;

        private TradeData(ResourceLocation structure, ResourceLocation marker, String name, int mapColor, int level, int minPrice, int maxPrice, int maxUses, int villagerXp) {
            this.structure = structure;
            this.marker = marker;
            this.mapName = name;
            this.mapColor = mapColor;
            this.level = level;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.maxUses = maxUses;
            this.villagerXp = villagerXp;
        }
    }


    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            Int2ObjectMap<List<VillagerTrades.ITrade>> trades = event.getTrades();
            for(TradeData data : customTrades){
                trades.get(data.level).add(new AdventureMapTrade(data));
            }
        }
    }


    private static class AdventureMapTrade implements VillagerTrades.ITrade {
        public final TradeData tradeData;

        private AdventureMapTrade(TradeData data) {
            this.tradeData = data;
        }

        @Override
        public MerchantOffer getOffer(@Nonnull Entity entity, @Nonnull Random random) {

            int i = random.nextInt(tradeData.maxPrice - tradeData.minPrice + 1) + tradeData.minPrice;

            ItemStack itemstack = createMap(entity.level, entity.blockPosition());
            if (itemstack.isEmpty()) return null;
            //int xp = xpFromTrade * Math.max(1, (info.level - 1));
            return new MerchantOffer(new ItemStack(Items.EMERALD, i), new ItemStack(Items.COMPASS), itemstack, tradeData.maxUses, tradeData.villagerXp, 0.2F);
        }

        private ItemStack createMap(World world, BlockPos pos) {
            Structure<?> structure = ForgeRegistries.STRUCTURE_FEATURES.getValue(new ResourceLocation("minecraft:igloo"));


            if (!(world instanceof ServerWorld) || structure == null) {
                return ItemStack.EMPTY;
            } else {
                BlockPos toPos = ((ServerWorld) world).findNearestMapFeature(structure, pos, SEARCH_RADIUS, true);
                if (toPos == null) {
                    return ItemStack.EMPTY;
                } else {
                    ItemStack stack = FilledMapItem.create(world, toPos.getX(), toPos.getZ(), (byte)2, true, true);
                    FilledMapItem.renderBiomePreviewMap((ServerWorld)world, stack);

                    //vanilla maps for backwards compat
                    if(structure instanceof OceanMonumentStructure){
                        MapData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MONUMENT);
                    }
                    else if(structure instanceof WoodlandMansionStructure){
                        MapData.addTargetDecoration(stack, pos, "+", MapDecoration.Type.MANSION);
                    }
                    else{
                        //adds custom deco
                        MapDecorationHandler.addTargetDecoration(stack, toPos, tradeData.marker, tradeData.mapColor);
                    }

                    stack.setHoverName(new TranslationTextComponent(tradeData.mapName));
                    return stack;
                }
            }
        }
    }

}
