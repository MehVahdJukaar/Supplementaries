package net.mehvahdjukaar.supplementaries.world.data;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class FlowerPotHelper {

    //maybe not needed since there's only 1 flower pot in vanilla and there are no mods that add more
    public static Set<Block> emptyPots;
    //vanilla pot flower pots
    //empty pot, map(flower item registry name, full block provider)
    public static Map<Block,Map<ResourceLocation, Supplier<? extends Block>> > fullPots;


    public static boolean isEmptyPot(Block b){
        //return (emptyPots!=null&&b!=null&&emptyPots.contains(b));
        return (fullPots!=null&&b!=null&& fullPots.containsKey(b));
    }



    public static void refresh(){
        emptyPots = new HashSet<>();
        for (Block b : ForgeRegistries.BLOCKS){
            if(b instanceof FlowerPotBlock){
                try {
                    Field f = ObfuscationReflectionHelper.findField(FlowerPotBlock.class, "emptyPot");
                    f.setAccessible(true);
                    FlowerPotBlock empty = ((Supplier<FlowerPotBlock>)f.get(b)).get();
                    emptyPots.add(empty);
                }
                catch (Exception ignored){};
            }
        }
        fullPots = Maps.newHashMap();
        for (Block pot : emptyPots) {
            try {
                Field f = ObfuscationReflectionHelper.findField(FlowerPotBlock.class, "fullPots");
                f.setAccessible(true);
                fullPots.put(pot,(Map<ResourceLocation, Supplier<? extends Block>>) f.get(pot));

                //Block block = fullPots.getOrDefault(((BlockItem) item).getBlock().getRegistryName(), Blocks.AIR.delegate).get();

            } catch (Exception ignored) {}
        }
        emptyPots.removeIf(pot -> !fullPots.containsKey(pot));
        //Supplementaries.LOGGER.info(fullPots.toString());
    }


    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        //TODO: might remove this on final release
        if(world instanceof ServerWorld){
            refresh();
        }
    }

}
