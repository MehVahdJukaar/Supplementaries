package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
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
    public static final TagKey<Block> FAUCET_CONNECTION_BLACKLIST = blockTag("faucet_connection_blacklist");
    public static final TagKey<Block> FAUCET_CONNECTION_WHITELIST = blockTag("faucet_connection_whitelist");
    public static final TagKey<Block> ROTATION_BLACKLIST = blockTag("un_rotatable");
    public static final TagKey<Block> BOMB_BREAKABLE = blockTag("bomb_breakable");
    public static final TagKey<Block> BRICK_BREAKABLE_GLASS = blockTag("brick_breakable_recursive");
    public static final TagKey<Block> BRICK_BREAKABLE_POTS = blockTag("brick_breakable");
    public static final TagKey<Block> FLINT_METALS = blockTag("flint_metals");
    public static final TagKey<Block> FRAME_BLOCK_BLACKLIST = blockTag("frame_block_blacklist");
    public static final TagKey<Block> LIGHTS_GUNPOWDER = blockTag("lights_gunpowder");
    public static final TagKey<Block> LIGHTABLE_BY_GUNPOWDER = blockTag("lightable_by_gunpowder");
    public static final TagKey<Block> PREVENTS_OFFSET_ABOVE = blockTag("prevents_offset_above");
    public static final TagKey<Block> TINTED_ON_MAPS_GENERIC = blockTag("map_tint_generic");
    public static final TagKey<Block> TINTED_ON_MAPS_GC = blockTag("map_tint_grass_color");
    public static final TagKey<Block> TINTED_ON_MAPS_FC = blockTag("map_tint_foliage_color");
    public static final TagKey<Block> TINTED_ON_MAPS_WC = blockTag("map_tint_water_color");
    public static final TagKey<Block> NOT_TINTED_ON_MAPS = blockTag("map_tint_blacklist");
    public static final TagKey<Block> FAST_FALL_ROPES = blockTag("fast_fall_climbable");
    public static final TagKey<Block> BOUNCY_BLOCKS = blockTag("bouncy_blocks");

    //item tags
    public static final TagKey<Item> SHULKER_BLACKLIST_TAG = itemTag("shulker_blacklist");
    public static final TagKey<Item> SLINGSHOT_BLACKLIST = itemTag("slingshot_blacklist");
    public static final TagKey<Item> SLINGSHOT_DAMAGEABLE = itemTag("slingshot_damageable");
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
    public static final TagKey<Item> KEYS = itemTag("keys");
    public static final TagKey<Item> STATUE_SWORDS = itemTag("statue_swords");
    public static final TagKey<Item> STATUE_TOOLS = itemTag("statue_tools");
    public static final TagKey<Item> SYRUP = itemTag("pancake_syrup");
    public static final TagKey<Item> OVERENCUMBERING = itemTag("overencumbering");
    public static final TagKey<Item> QUIVER_BLACKLIST = itemTag("quiver_blacklist");
    public static final TagKey<Item> IGNITE_FLINT_BLOCKS = itemTag("ignite_flint_blocks");

    public static final TagKey<Item> SHULKER_BOXES = CitemTag("shulker_boxes");

    //entity tags
    public static final TagKey<EntityType<?>> JAR_CATCHABLE = entityTag("jar_catchable");
    public static final TagKey<EntityType<?>> JAR_BABY_CATCHABLE = entityTag("jar_baby_catchable");
    public static final TagKey<EntityType<?>> CAGE_CATCHABLE = entityTag("cage_catchable");
    public static final TagKey<EntityType<?>> CAGE_BABY_CATCHABLE = entityTag("cage_baby_catchable");
    public static final TagKey<EntityType<?>> CAPTURE_BLACKLIST = entityTag("capture_blacklist");
    public static final TagKey<EntityType<?>> NON_ANGERABLE = entityTag("non_angerable");
    public static final TagKey<EntityType<?>> FLUTE_PET = entityTag("flute_pet");
    public static final TagKey<EntityType<?>> EATS_FODDER = entityTag("eats_fodder");
    public static final TagKey<EntityType<?>> ROTATABLE = entityTag("rotatable");
    public static final TagKey<EntityType<?>> URN_SPAWN = entityTag("urn_spawn");
    public static final TagKey<EntityType<?>> AWNING_BLACKLIST = entityTag("cant_bounce_off_awnings");

    //features
    public static final TagKey<Structure> WAY_SIGN_DESTINATIONS = structureTag("way_sign_destinations");
    public static final TagKey<Structure> ADVENTURE_MAP_DESTINATIONS = structureTag("adventure_map_destinations");

    public static final TagKey<Potion> QUIVER_POTION_BLACKLIST = potionTag("quiver_blacklist");
    public static final TagKey<Potion> TIPPED_SPIKES_POTION_BLACKLIST = potionTag("tipped_spikes_blacklist");


    //biomes
    public static final TagKey<Biome> HAS_WAY_SIGNS = biomeTag("has_way_signs");
    public static final TagKey<Biome> HAS_CAVE_URNS = biomeTag("has_cave_urns");
    public static final TagKey<Biome> HAS_WILD_FLAX = biomeTag("has_wild_flax");
    public static final TagKey<Biome> HAS_BASALT_ASH = biomeTag("has_basalt_ash");

    private static TagKey<Structure> MCstructureTag(String name) {
        return TagKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace(name));
    }
    private static TagKey<Structure> structureTag(String name) {
        return TagKey.create(Registries.STRUCTURE, Supplementaries.res(name));
    }
    private static TagKey<Potion> potionTag(String name) {
        return TagKey.create(Registries.POTION, Supplementaries.res(name));
    }
    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Supplementaries.res(name));
    }

    private static TagKey<Item> CitemTag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, Supplementaries.res(name));
    }

    private static TagKey<EntityType<?>> entityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Supplementaries.res(name));
    }

    private static TagKey<DamageType> damageTag(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, Supplementaries.res(name));
    }

    private static TagKey<Enchantment> enchTag(String name) {
        return TagKey.create(Registries.ENCHANTMENT, Supplementaries.res(name));
    }

    private static TagKey<Biome> biomeTag(String name) {
        return TagKey.create(Registries.BIOME, Supplementaries.res(name));
    }

}
