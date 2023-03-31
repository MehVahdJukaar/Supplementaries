package net.mehvahdjukaar.supplementaries.common.utils.forge;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class FlowerPotHandlerImpl {
    //vanilla pot flower pots
    //empty pot, map(flower item registry name, full block provider)
    private static Map<Block, Map<ResourceLocation, Supplier<? extends Block>>> FULL_POTS;

    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        return FULL_POTS.get(emptyPot.getEmptyPot()).getOrDefault(Utils.getID(flowerBlock), () -> Blocks.AIR).get();
    }

    public static boolean isEmptyPot(Block b) {
        //return (emptyPots!=null&&b!=null&&emptyPots.contains(b));
        return (FULL_POTS != null && b != null && FULL_POTS.containsKey(b));
    }

    //move to forge
    public static void setup() {
        //registers pots
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(Utils.getID(ModRegistry.FLAX_ITEM.get()), ModRegistry.FLAX_POT);

        //maybe not needed since there's only 1 flower pot in vanilla and there are no mods that add more
        Set<FlowerPotBlock> emptyPots = new HashSet<>();
        for (Block b : ForgeRegistries.BLOCKS) {
            if (b instanceof FlowerPotBlock flowerPotBlock) {
                emptyPots.add(flowerPotBlock.getEmptyPot());
            }
        }
        FULL_POTS = new IdentityHashMap<>();
        for (FlowerPotBlock pot : emptyPots) {
            FULL_POTS.put(pot, pot.getFullPotsView());
        }
    }

    public static Block getEmptyPot(FlowerPotBlock fullPot) {
        return fullPot.getEmptyPot();
    }

}
