package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.HashMap;
import java.util.Map;

public class ComposterHelper {
    private static final Map<ItemLike, Float> COMPOSTER_ADDITIONS = new HashMap<>();
    public static final float LEAVES = 0.3F;
    public static final float SAPLING = 0.3F;
    public static final float SEEDS = 0.3F;
    public static final float GRASS = 0.3F;
    public static final float BERRIES = 0.3F;
    public static final float DOUBLE_GRASS = 0.5F;
    public static final float REEDS = 0.5F;
    public static final float VINE = 0.5F;
    public static final float SMALL_FRUIT = 0.5F;
    public static final float LILY = 0.65F;
    public static final float FRUIT = 0.65F;
    public static final float CROP = 0.65F;
    public static final float MUSHROOM = 0.65F;
    public static final float FLOWER = 0.65F;
    public static final float FERN = 0.65F;
    public static final float DOUBLE_FLOWER = 0.65F;
    public static final float DOUBLE_FERN = 0.65F;
    public static final float HAY_BLOCK = 0.85F;
    public static final float HAY_SLAB = 0.425F;
    public static final float HAY_STAIRS = 0.56100005F;
    public static final float HAY_FLOOR = 0.2125F;
    public static final float BREAD = 0.85F;
    public static final float CAKE = 1.0F;

    public static void addCompostable(ItemLike item, float chance) {
        COMPOSTER_ADDITIONS.put(item, chance);
    }

    public static void registerAllPreparedCompostables() {
        COMPOSTER_ADDITIONS.forEach((itemProvider, chance) -> {
            ComposterBlock.COMPOSTABLES.put(itemProvider.asItem(), chance);
        });
    }
}
