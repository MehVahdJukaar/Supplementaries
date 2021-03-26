package net.mehvahdjukaar.supplementaries.common;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


public class FlowerPotHelper {

    //vanilla pot flower pots
    //empty pot, map(flower item registry name, full block provider)
    private static Map<Block,Map<ResourceLocation, Supplier<? extends Block>> > FULL_POTS;

    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock){
        return FULL_POTS.get(emptyPot.getEmptyPot()).getOrDefault(flowerBlock.getRegistryName(), Blocks.AIR.delegate).get();
    }

    public static boolean isEmptyPot(Block b){
        //return (emptyPots!=null&&b!=null&&emptyPots.contains(b));
        return (FULL_POTS !=null&&b!=null&& FULL_POTS.containsKey(b));
    }

    public static void init(){
        //maybe not needed since there's only 1 flower pot in vanilla and there are no mods that add more
        Set<FlowerPotBlock> emptyPots = new HashSet<>();
        for (Block b : ForgeRegistries.BLOCKS){
            if(b instanceof FlowerPotBlock){
                emptyPots.add(((FlowerPotBlock) b).getEmptyPot());
            }
        }
        FULL_POTS = Maps.newHashMap();
        for (FlowerPotBlock pot : emptyPots) {

            try {
                Field f = ObfuscationReflectionHelper.findField(FlowerPotBlock.class, "fullPots");
                f.setAccessible(true);
                FULL_POTS.put(pot,(Map<ResourceLocation, Supplier<? extends Block>>) f.get(pot));

                //Block block = fullPots.getOrDefault(((BlockItem) item).getBlock().getRegistryName(), Blocks.AIR.delegate).get();

            } catch (Exception ignored) {
                Supplementaries.LOGGER.info("Failed to create flower pots");
            }
        }
        emptyPots.removeIf(pot -> !FULL_POTS.containsKey(pot));
        //Supplementaries.LOGGER.info(fullPots.toString());
    }
}
