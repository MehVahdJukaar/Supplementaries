package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IFlowerModelProvider;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class FlowerPotHandler {

    //vanilla pot flower pots
    //empty pot, map(flower item registry name, full block provider)
    private static Map<Block, Map<ResourceLocation, Supplier<? extends Block>>> FULL_POTS;

    private static final List<BlockState> FULL_POTs_BLOCKSTATES_LIST = new ArrayList<>();

    public static BlockState getAprilPot() {
        int ind = (int) ((System.currentTimeMillis() / 15000) % FULL_POTs_BLOCKSTATES_LIST.size());
        return FULL_POTs_BLOCKSTATES_LIST.get(ind);
    }

    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        return FULL_POTS.get(emptyPot.getEmptyPot()).getOrDefault(flowerBlock.getRegistryName(), Blocks.AIR.delegate).get();
    }

    public static boolean isEmptyPot(Block b) {
        //return (emptyPots!=null&&b!=null&&emptyPots.contains(b));
        return (FULL_POTS != null && b != null && FULL_POTS.containsKey(b));
    }

    public static void init() {
        //registers pots
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModRegistry.FLAX_ITEM.get().getRegistryName(), ModRegistry.FLAX_POT);

        //maybe not needed since there's only 1 flower pot in vanilla and there are no mods that add more
        Set<FlowerPotBlock> emptyPots = new HashSet<>();
        for (Block b : ForgeRegistries.BLOCKS) {
            if (b instanceof FlowerPotBlock) {
                emptyPots.add(((FlowerPotBlock) b).getEmptyPot());
            }
        }
        FULL_POTS = Maps.newHashMap();
        for (FlowerPotBlock pot : emptyPots) {
            FULL_POTS.put(pot,pot.getFullPotsView());
            FULL_POTs_BLOCKSTATES_LIST.addAll((pot.getFullPotsView()).values().stream().map(s -> s.get().defaultBlockState()).collect(Collectors.toList()));
        }
    }

    //flower box
    private static final Map<Item, ResourceLocation> SPECIAL_FLOWER_BOX_FLOWERS = new HashMap<>();

    /**
     * for mods: use this or #Link(IFlowerModelProvider) to register plants that go into a flower box and have a custom model
     *
     * @param item  target item
     * @param model resource location of the block model to be used
     */
    public static void registerCustomFlower(Item item, ResourceLocation model) {
        SPECIAL_FLOWER_BOX_FLOWERS.put(item, model);
    }

    private static void registerCompatFlower(String itemRes) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRes));
        if (item != null && item != Items.AIR) {
            ResourceLocation res = Supplementaries.res("block/plants/" + item.getRegistryName().getPath());
            CUSTOM_MODELS.add(res);
            registerCustomFlower(item, res);

        }
    }

    public static final List<ResourceLocation> CUSTOM_MODELS = new ArrayList<>();

    //static registerBus for client and server sync
    static{
        List<String> toAdd = new ArrayList<>();
        toAdd.add(Items.CACTUS.getRegistryName().toString());
        toAdd.add(Items.FLOWERING_AZALEA.getRegistryName().toString());
        toAdd.add(Items.AZALEA.getRegistryName().toString());
        toAdd.add(ModRegistry.FLAX_SEEDS_ITEM.get().getRegistryName().toString());
        if(ModList.get().isLoaded("snowyspirit")){
            toAdd.add("snowyspirit:ginger_flower");
        }

        if(CompatHandler.quark) {
            Item[] items = new Item[]{
                    Items.SUGAR_CANE, Items.BEETROOT_SEEDS, Items.CARROT, Items.CHORUS_FLOWER, Items.POTATO, Items.GRASS,
                    Items.COCOA_BEANS, Items.WHEAT_SEEDS, Items.VINE, Items.LARGE_FERN, Items.SWEET_BERRIES, Items.WEEPING_VINES,
                    Items.NETHER_SPROUTS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TALL_GRASS, Items.SEA_PICKLE, Items.NETHER_WART
            };
            toAdd.add("quark:chorus_weeds");
            toAdd.add("quark:root");
            toAdd.add("quark:chorus_twist");
            Arrays.stream(items).forEach(i -> toAdd.add(i.getRegistryName().toString()));
        }

        if(CompatHandler.pokecube_legends){
            toAdd.add("pokecube_legends:crystallized_cactus");
        }

        if(CompatHandler.pokecube){
            String[] berries = new String[]{
                "pokecube:berry_aspear",
                "pokecube:berry_cheri",
                "pokecube:berry_chesto",
                "pokecube:berry_cornn",
                "pokecube:berry_enigma",
                "pokecube:berry_grepa",
                "pokecube:berry_hondew",
                "pokecube:berry_jaboca",
                "pokecube:berry_kelpsy",
                "pokecube:berry_leppa",
                "pokecube:berry_lum",
                "pokecube:berry_nanab",
                "pokecube:berry_null",
                "pokecube:berry_oran",
                "pokecube:berry_pecha",
                "pokecube:berry_persim",
                "pokecube:berry_pinap",
                "pokecube:berry_pomeg",
                "pokecube:berry_qualot",
                "pokecube:berry_rawst",
                "pokecube:berry_rowap",
                "pokecube:berry_sitrus",
                "pokecube:berry_tamato"};
            toAdd.addAll(Arrays.asList(berries));
        }

        if(CompatHandler.moreminecarts){
            toAdd.add("moreminecarts:chunkrodite_block");
            toAdd.add("moreminecarts:glass_cactus");
        }
        if(CompatHandler.habitat){
            toAdd.add("habitat:rafflesia");
            toAdd.add("habitat:orange_ball_cactus");
            toAdd.add("habitat:red_ball_cactus");
            toAdd.add("habitat:pink_ball_cactus");
            toAdd.add("habitat:yellow_ball_cactus");
            toAdd.add("habitat:kabloom_pulp");
        }
        if(CompatHandler.endergetic){
            toAdd.add("endergetic:tall_poise_bush");
        }
        if(CompatHandler.simplefarming){
            toAdd.add("simplefarming:cantaloupe_block");
            toAdd.add("simplefarming:honeydew_block");
            toAdd.add("simplefarming:squash_block");
        }
        if(CompatHandler.atmospheric){
            toAdd.add("atmospheric:barrel_cactus");
        }

        //flower box

        toAdd.forEach(FlowerPotHandler::registerCompatFlower);

    }

    @Nullable
    public static ResourceLocation getSpecialFlowerModel(Item i) {
        ResourceLocation res = SPECIAL_FLOWER_BOX_FLOWERS.get(i);

        if (res != null) return res;

        if (i instanceof IFlowerModelProvider flowerModelProvider) {
            return flowerModelProvider.getModel();
        } else if (i instanceof BlockItem blockItem && blockItem.getBlock() instanceof IFlowerModelProvider flowerModelProvider) {
            return flowerModelProvider.getModel();
        }
        return null;
    }

    public static boolean hasSpecialFlowerModel(Item i) {
        return getSpecialFlowerModel(i) != null;
    }

}
