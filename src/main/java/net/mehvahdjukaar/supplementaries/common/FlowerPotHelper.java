package net.mehvahdjukaar.supplementaries.common;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IFlowerModelProvider;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class FlowerPotHelper {

    //vanilla pot flower pots
    //empty pot, map(flower item registry name, full block provider)
    private static Map<Block, Map<ResourceLocation, Supplier<? extends Block>>> FULL_POTS;

    private static final List<BlockState> FULL_POT_LIST = new ArrayList<>();

    public static BlockState getAprilPot() {
        int ind = (int) ((System.currentTimeMillis() / 15000) % FULL_POT_LIST.size());
        return FULL_POT_LIST.get(ind);
    }

    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        return FULL_POTS.get(emptyPot.getEmptyPot()).getOrDefault(flowerBlock.getRegistryName(), Blocks.AIR.delegate).get();
    }

    public static boolean isEmptyPot(Block b) {
        //return (emptyPots!=null&&b!=null&&emptyPots.contains(b));
        return (FULL_POTS != null && b != null && FULL_POTS.containsKey(b));
    }

    public static void init() {
        //maybe not needed since there's only 1 flower pot in vanilla and there are no mods that add more
        Set<FlowerPotBlock> emptyPots = new HashSet<>();
        for (Block b : ForgeRegistries.BLOCKS) {
            if (b instanceof FlowerPotBlock) {
                emptyPots.add(((FlowerPotBlock) b).getEmptyPot());
            }
        }
        FULL_POTS = Maps.newHashMap();
        for (FlowerPotBlock pot : emptyPots) {

            try {
                Field f = ObfuscationReflectionHelper.findField(FlowerPotBlock.class, "fullPots");
                f.setAccessible(true);
                FULL_POTS.put(pot, (Map<ResourceLocation, Supplier<? extends Block>>) f.get(pot));
                FULL_POT_LIST.addAll(((Map<ResourceLocation, Supplier<? extends Block>>) f.get(pot)).values().stream().map(s -> s.get().defaultBlockState()).collect(Collectors.toList()));

                //Block block = fullPots.getOrDefault(((BlockItem) item).getBlock().getRegistryName(), Blocks.AIR.delegate).get();

            } catch (Exception ignored) {
                Supplementaries.LOGGER.info("Failed to create flower pots");
            }
        }
        //emptyPots.removeIf(pot -> !FULL_POTS.containsKey(pot));
        //Supplementaries.LOGGER.info(fullPots.toString());

    }

    //flower box
    private static final Map<Item, ResourceLocation> SPECIAL_FLOWER_BOX_FLOWERS = new HashMap<>();

    /**
     * for mods: use this or #Link(IFlowerModelProvider) to register plants that go into a flower box and have a custom model
     *
     * @param item  target item
     * @param model resource location of the block model to be used. You can use ModelResourceLocation
     */
    public static void registerCustomFlower(Item item, ResourceLocation model) {
        SPECIAL_FLOWER_BOX_FLOWERS.put(item, model);
    }

    private static void registerCompatFlower(String itemRes) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRes));
        if (item != null && item != Items.AIR) {
            ResourceLocation res = Supplementaries.res("plants/" + item.getRegistryName().getPath());
            CUSTOM_MODELS.add(res);
            registerCustomFlower(item, res);

        }
    }

    public static final List<ResourceLocation> CUSTOM_MODELS = new ArrayList<>();

    //static init for client and server sync
    static{
        List<String> toAdd = new ArrayList<>();
        toAdd.add(Items.CACTUS.getRegistryName().toString());
        toAdd.add(ModRegistry.FLAX_SEEDS_ITEM.get().getRegistryName().toString());
        if(CompatHandler.quark) {
            Item[] items = new Item[]{
                    Items.SUGAR_CANE, Items.BEETROOT_SEEDS, Items.CARROT, Items.CHORUS_FLOWER, Items.POTATO, Items.GRASS,
                    Items.COCOA_BEANS, Items.WHEAT_SEEDS, Items.VINE, Items.LARGE_FERN, Items.SWEET_BERRIES,
                    Items.NETHER_SPROUTS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TALL_GRASS
            };
            toAdd.add("quark:chorus_weeds");
            toAdd.add("quark:root");
            toAdd.add("quark:chorus_twist");

            Arrays.stream(items).forEach(i -> toAdd.add(i.getRegistryName().toString()));
        }
        //flower box

        toAdd.forEach(FlowerPotHelper::registerCompatFlower);

    }

    public static void registerCustomModels(Consumer<String> registerFunction){
        CUSTOM_MODELS.stream().map(ResourceLocation::toString).forEach(registerFunction);
    }


    @Nullable
    public static ResourceLocation getSpecialFlowerModel(Item i) {
        ResourceLocation res = SPECIAL_FLOWER_BOX_FLOWERS.get(i);

        if (res != null) return res;

        if (i instanceof IFlowerModelProvider) {
            return ((IFlowerModelProvider) i).getModel();
        } else if (i instanceof BlockItem && ((BlockItem) i).getBlock() instanceof IFlowerModelProvider) {
            return ((IFlowerModelProvider) ((BlockItem) i).getBlock()).getModel();
        }
        return null;
    }

    public static boolean hasSpecialFlowerModel(Item i) {
        return getSpecialFlowerModel(i) != null;
    }

}
