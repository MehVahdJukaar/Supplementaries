package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ILightable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class ModTags {

    //block tags
    public static final TagKey<Block> POSTS = blockTag("posts");
    public static final TagKey<Block> PALISADES = blockTag("palisades");
    public static final TagKey<Block> BEAMS = blockTag("beams");
    public static final TagKey<Block> WALLS = blockTag("walls");
    public static final TagKey<Block> ROPE_SUPPORT_TAG = blockTag("rope_support");
    public static final TagKey<Block> ROPE_HANG_TAG = blockTag("hang_from_ropes");
    public static final TagKey<Block> BELLOWS_TICKABLE_TAG = blockTag("bellows_tickable");
    public static final TagKey<Block> WATER_HOLDER = blockTag("water_holder");
    public static final TagKey<Block> POURING_TANK = blockTag("pouring_tank");
    public static final TagKey<Block> VINE_SUPPORT = blockTag("vine_support");
    public static final TagKey<Block> PANE_CONNECTION = blockTag("pane_connection");
    public static final TagKey<Block> CONCRETE_POWDERS = blockTag("concrete_powders");
    public static final TagKey<Block> ROTATION_BLACKLIST = blockTag("un_rotatable");
    public static final TagKey<Block> BOMB_BREAKABLE = blockTag("bomb_breakable");
    //item tags
    public static final TagKey<Item> SHULKER_BLACKLIST_TAG = itemTag("shulker_blacklist");
    public static final TagKey<Item> COOKIES = itemTag("cookies");
    public static final TagKey<Item> BRICKS = itemTag("throwable_bricks");
    public static final TagKey<Item> ROPES = itemTag("ropes");
    public static final TagKey<Item> CHAINS = itemTag("chains");
    public static final TagKey<Item> PEDESTAL_UPRIGHT = itemTag("pedestal_upright");
    public static final TagKey<Item> PEDESTAL_DOWNRIGHT = itemTag("pedestal_downright");
    public static final TagKey<Item> CHOCOLATE_BARS = itemTag("chocolate_bars");
    public static final TagKey<Item> FLINT_AND_STEELS = ILightable.FLINT_AND_STEELS;
    public static final TagKey<Item> FLOWER_BOX_PLANTABLE = itemTag("flower_box_plantable");
    public static final TagKey<Item> CHALK = itemTag("chalk");
    public static final TagKey<Item> BOOKS = itemTag("placeable_books");
    public static final TagKey<Item> DUSTS = itemTag("hourglass_dusts");
    public static final TagKey<Item> SANDS = itemTag("hourglass_sands");
    public static final TagKey<Item> KEY = itemTag("key");
    public static final TagKey<Item> STATUE_SWORDS = itemTag("statue_swords");
    public static final TagKey<Item> STATUE_TOOLS = itemTag("statue_tools");
    public static final TagKey<Item> PRESENTS = itemTag("presents");
    public static final TagKey<Item> SYRUP = itemTag("pancake_syrup");
    public static final TagKey<Item> SHULKER_BOXES = ItemTags.create(new ResourceLocation("forge", "shulker_boxes"));
    //entity tags
    public static final TagKey<EntityType<?>> JAR_CATCHABLE = entityTag("jar_catchable");
    public static final TagKey<EntityType<?>> TINTED_JAR_CATCHABLE = entityTag("jar_tinted_catchable");
    public static final TagKey<EntityType<?>> CAGE_CATCHABLE = entityTag("cage_catchable");
    public static final TagKey<EntityType<?>> CAGE_BABY_CATCHABLE = entityTag("cage_baby_catchable");
    public static final TagKey<EntityType<?>> FLUTE_PET = entityTag("flute_pet");
    public static final TagKey<EntityType<?>> EATS_FODDER = entityTag("eats_fodder");
    //features
    public static final TagKey<ConfiguredStructureFeature<?, ?>> WAY_SIGN_DESTINATIONS = structureTag("way_sign_destinations");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> ADVENTURE_MAP_DESTINATIONS = structureTag("adventure_map_destinations");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> BASTION_REMNANT = structureTag("bastion_remnant");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> DESERT_PYRAMID = structureTag("desert_pyramid");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> END_CITY = structureTag("end_city");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> NETHER_FORTRESS = structureTag("fortress");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> IGLOO = structureTag("igloo");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> JUNGLE_TEMPLE = structureTag("jungle_pyramid");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> PILLAGER_OUTPOST = structureTag("pillager_outpost");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> SWAMP_HUT = structureTag("swamp_hut");


    //biomes
    public static final TagKey<Biome> HAS_WAY_SIGNS = biomeTag("has_way_signs");

    private static TagKey<ConfiguredStructureFeature<?, ?>> structureTag(String name) {
        return TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, Supplementaries.res(name));
    }

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, Supplementaries.res(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, Supplementaries.res(name));
    }

    private static TagKey<EntityType<?>> entityTag(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, Supplementaries.res(name));
    }

    private static TagKey<Biome> biomeTag(String name) {
        return TagKey.create(Registry.BIOME_REGISTRY, Supplementaries.res(name));
    }

}
