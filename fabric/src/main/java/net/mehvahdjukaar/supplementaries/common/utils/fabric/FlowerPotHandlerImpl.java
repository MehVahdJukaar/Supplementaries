package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FlowerPotHandlerImpl {

    public static void setup() {
    }

    public static boolean isEmptyPot(Block b) {
        return b == Blocks.FLOWER_POT;
    }

    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        return FlowerPotBlock.POTTED_BY_CONTENT.get(flowerBlock);
    }
}
