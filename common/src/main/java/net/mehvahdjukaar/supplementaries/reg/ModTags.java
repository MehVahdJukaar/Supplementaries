package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
    public static final TagKey<Block> BRICK_BREAKABLE_GLASS = blockTag("brick_breakable");
    public static final TagKey<Block> FLINT_METALS = blockTag("flint_metals");
    public static final TagKey<Block> WALL_LANTERNS_BLACKLIST = blockTag("wall_lanterns_blacklist");
    public static final TagKey<Block> WALL_LANTERNS_WHITELIST = blockTag("wall_lanterns_whitelist");
    //item tags
    public static final TagKey<Item> SHULKER_BLACKLIST_TAG = itemTag("shulker_blacklist");
    public static final TagKey<Item> SLINGSHOT_BLACKLIST = itemTag("slingshot_blacklist");
    public static final TagKey<Item> COOKIES = itemTag("cookies");
    public static final TagKey<Item> BRICKS = itemTag("throwable_bricks");
    public static final TagKey<Item> ROPES = itemTag("ropes");
    public static final TagKey<Item> CHAINS = itemTag("chains");
    public static final TagKey<Item> PEDESTAL_UPRIGHT = itemTag("pedestal_upright");
    public static final TagKey<Item> PEDESTAL_DOWNRIGHT = itemTag("pedestal_downright");
    public static final TagKey<Item> CHOCOLATE_BARS = itemTag("chocolate_bars");
    public static final TagKey<Item> FLINT_AND_STEELS = ILightable.FLINT_AND_STEELS;
    public static final TagKey<Item> FLOWER_BOX_PLANTABLE = itemTag("flower_box_plantable");
    public static final Map<DyeColor, TagKey<Item>> BLACKBOARD_TAGS = Arrays.stream(DyeColor.values())
            .collect(Collectors.toUnmodifiableMap(d -> d, d -> itemTag("blackboard_" + d.getName())));
    public static final TagKey<Item> BOOKS = itemTag("placeable_books");
    public static final TagKey<Item> DUSTS = itemTag("hourglass_dusts");
    public static final TagKey<Item> SANDS = itemTag("hourglass_sands");
    public static final TagKey<Item> KEY = itemTag("key");
    public static final TagKey<Item> STATUE_SWORDS = itemTag("statue_swords");
    public static final TagKey<Item> STATUE_TOOLS = itemTag("statue_tools");
    public static final TagKey<Item> PRESENTS = itemTag("presents");
    public static final TagKey<Item> SYRUP = itemTag("pancake_syrup");

    //entity tags
    public static final TagKey<EntityType<?>> JAR_CATCHABLE = entityTag("jar_catchable");
    public static final TagKey<EntityType<?>> JAR_BABY_CATCHABLE = entityTag("jar_baby_catchable");
    public static final TagKey<EntityType<?>> CAGE_CATCHABLE = entityTag("cage_catchable");
    public static final TagKey<EntityType<?>> CAGE_BABY_CATCHABLE = entityTag("cage_baby_catchable");
    public static final TagKey<EntityType<?>> FLUTE_PET = entityTag("flute_pet");
    public static final TagKey<EntityType<?>> EATS_FODDER = entityTag("eats_fodder");
    public static final TagKey<EntityType<?>> ROTATABLE = entityTag("rotatable");
    public static final TagKey<EntityType<?>> URN_SPAWN = entityTag("urn_spawn");
    //features
    public static final TagKey<Structure> WAY_SIGN_DESTINATIONS = structureTag("way_sign_destinations");
    public static final TagKey<Structure> ADVENTURE_MAP_DESTINATIONS = structureTag("adventure_map_destinations");
    public static final TagKey<Structure> BASTION_REMNANT = MCstructureTag("bastion_remnant");
    public static final TagKey<Structure> DESERT_PYRAMID = MCstructureTag("desert_pyramid");
    public static final TagKey<Structure> END_CITY = MCstructureTag("end_city");
    public static final TagKey<Structure> NETHER_FORTRESS = MCstructureTag("fortress");
    public static final TagKey<Structure> IGLOO = MCstructureTag("igloo");
    public static final TagKey<Structure> ANCIENT_CITY = MCstructureTag("ancient_city");
    public static final TagKey<Structure> JUNGLE_TEMPLE = MCstructureTag("jungle_pyramid");
    public static final TagKey<Structure> PILLAGER_OUTPOST = MCstructureTag("pillager_outpost");
    public static final TagKey<Structure> SWAMP_HUT = MCstructureTag("swamp_hut");


    //biomes
    public static final TagKey<Biome> HAS_WAY_SIGNS = biomeTag("has_way_signs");
    public static final TagKey<Biome> HAS_CAVE_URNS = biomeTag("has_cave_urns");
    public static final TagKey<Biome> HAS_WILD_FLAX = biomeTag("has_wild_flax");
    public static final TagKey<Biome> HAS_BASALT_ASH = biomeTag("has_basalt_ash");

    private static TagKey<Structure> MCstructureTag(String name) {
        return TagKey.create(Registry.STRUCTURE_REGISTRY, Supplementaries.res(name));
    }
    private static TagKey<Structure> structureTag(String name) {
        return TagKey.create(Registry.STRUCTURE_REGISTRY, Supplementaries.res(name));
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
