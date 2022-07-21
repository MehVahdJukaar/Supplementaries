package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder.LIST_STRING_CHECK;

public class ServerConfigs {

    public static void init(){};

    public static ConfigSpec SERVER_SPEC;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Supplementaries.res("common"), ConfigType.COMMON);

        Blocks.init(builder);
        Spawns.init(builder);
        Tweaks.init(builder);
        Items.init(builder);
        General.init(builder);

        builder.setSynced();
        builder.onChange(ServerConfigs::onRefresh);

        SERVER_SPEC = builder.buildAndRegister();
    }

    private static void onRefresh(){
        ResourceLocation res = new ResourceLocation(Items.ROPE_ARROW_ROPE.get());
        var opt = Registry.BLOCK.getHolder(ResourceKey.create(Registry.BLOCK.key(), res));
        Items.ROPE_ARROW_OVERRIDE = (Holder.Reference<Block>)opt.orElse(Registry.BLOCK.getHolder(ResourceKey.create(Registry.BLOCK.key(), Supplementaries.res("rope"))).get());
    }

    public static class Items {
        public static Holder.Reference<Block> ROPE_ARROW_OVERRIDE = null;

        public static Supplier<Integer> ROPE_ARROW_CAPACITY;
        public static Supplier<Boolean> ROPE_ARROW_CROSSBOW;
        public static Supplier<String> ROPE_ARROW_ROPE;
        public static Supplier<Integer> FLUTE_RADIUS;
        public static Supplier<Integer> FLUTE_DISTANCE;
        public static Supplier<Double> BOMB_RADIUS;
        public static Supplier<Integer> BOMB_FUSE;
        public static Supplier<BombEntity.BreakingMode> BOMB_BREAKS;
        public static Supplier<Double> BOMB_BLUE_RADIUS;
        public static Supplier<BombEntity.BreakingMode> BOMB_BLUE_BREAKS;
        public static Supplier<Double> SLINGSHOT_RANGE;
        public static Supplier<Integer> SLINGSHOT_CHARGE;
        public static Supplier<Double> SLINGSHOT_DECELERATION;
        public static Supplier<Boolean> UNRESTRICTED_SLINGSHOT;
        public static Supplier<Hands> WRENCH_BYPASS;
        public static Supplier<Integer> BUBBLE_BLOWER_COST;

        private static void init(ConfigBuilder builder) {
            builder.push("items");

            builder.push("bubble_blower");
            BUBBLE_BLOWER_COST = builder.comment("Amount of soap consumed per bubble block placed")
                    .define("stasis_cost", 5, 1, 25);
            builder.pop();

            builder.push("wrench");
            WRENCH_BYPASS = builder.comment("Allows wrenches to bypass a block interaction action prioritizing their own when on said hand")
                    .define("bypass_when_on", Hands.MAIN_HAND);
            builder.pop();

            //rope arrow
            builder.push("rope_arrow");
            ROPE_ARROW_ROPE = builder.comment("If you don't like my ropes you can specify here the block id of" +
                            "a rope from another mod which will get deployed by rope arrows instead of mine")
                    .define("rope_arrow_override", "supplementaries:rope");
            ROPE_ARROW_CAPACITY = builder.comment("Max number of robe items allowed to be stored inside a rope arrow")
                    .define("capacity", 32, 1, 256);
            ROPE_ARROW_CROSSBOW = builder.comment("Makes rope arrows exclusive to crossbows")
                    .define("exclusive_to_crossbows", false);
            builder.pop();
            //flute
            builder.push("flute");
            FLUTE_RADIUS = builder.comment("Radius in which an unbound flute will search pets")
                    .define("unbound_radius", 64, 0, 500);
            FLUTE_DISTANCE = builder.comment("Max distance at which a bound flute will allow a pet to teleport")
                    .define("bound_distance", 64, 0, 500);

            builder.pop();
            //bomb
            builder.push("bomb");
            BOMB_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .define("explosion_radius", 2, 0.1, 10);
            BOMB_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .define("break_blocks", BombEntity.BreakingMode.WEAK);
            BOMB_FUSE = builder.comment("Put here any number other than 0 to have your bombs explode after a certaom amount of ticks instead than on contact")
                    .define("bomb_fuse", 0, 0, 100000);
            builder.pop();

            builder.push("blue_bomb");
            BOMB_BLUE_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .define("explosion_radius", 5.15, 0.1, 10);
            BOMB_BLUE_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .define("break_blocks", BombEntity.BreakingMode.WEAK);

            builder.pop();

            builder.push("slingshot");
            SLINGSHOT_RANGE = builder.comment("Slingshot range multiplier. Affect the initial projectile speed")
                    .define("range_multiplier", 1f, 0, 5);
            SLINGSHOT_CHARGE = builder.comment("Time in ticks to fully charge a slingshot")
                    .define("charge_time", 20, 0, 100);
            SLINGSHOT_DECELERATION = builder.comment("Deceleration for the stasis projectile")
                    .define("stasis_deceleration", 0.9625, 0.1, 1);
            UNRESTRICTED_SLINGSHOT = builder.comment("Allow enderman to intercept any slingshot projectile")
                    .define("unrestricted_enderman_intercept", true);
            builder.pop();


            builder.pop();
        }

    }

    public enum Hands {
        MAIN_HAND, OFF_HAND, BOTH, NONE
    }

    public static class Tweaks {
        public static Supplier<Boolean> ENDER_PEAR_DISPENSERS;
        public static Supplier<Boolean> AXE_DISPENSER_BEHAVIORS;
        public static Supplier<Boolean> DIRECTIONAL_CAKE;
        public static Supplier<Boolean> DOUBLE_CAKE_PLACEMENT;
        public static Supplier<Boolean> HANGING_POT_PLACEMENT;
        public static Supplier<Boolean> WALL_LANTERN_PLACEMENT;
        public static Supplier<Boolean> WALL_LANTERN_HIGH_PRIORITY;
        public static Supplier<Boolean> THROWABLE_BRICKS_ENABLED;
        public static Supplier<List<String>> WALL_LANTERN_BLACKLIST;
        public static Supplier<LightableLanternBlock.FallMode> FALLING_LANTERNS;
        public static Supplier<Boolean> BELL_CHAIN;
        public static Supplier<Integer> BELL_CHAIN_LENGTH;
        public static Supplier<Boolean> PLACEABLE_STICKS;
        public static Supplier<Boolean> PLACEABLE_RODS;
        public static Supplier<Boolean> RAKED_GRAVEL;
        public static Supplier<Boolean> BOTTLE_XP;
        public static Supplier<Integer> BOTTLING_COST;
        public static Supplier<List<? extends List<String>>> CUSTOM_ADVENTURER_MAPS_TRADES;
        public static Supplier<Boolean> RANDOM_ADVENTURER_MAPS;
        public static Supplier<Boolean> MAP_MARKERS;
        public static Supplier<Boolean> CEILING_BANNERS;
        public static Supplier<Boolean> PLACEABLE_BOOKS;
        public static Supplier<Boolean> WRITTEN_BOOKS;
        public static Supplier<Double> BOOK_POWER;
        public static Supplier<Double> ENCHANTED_BOOK_POWER;
        public static Supplier<Boolean> ZOMBIE_HORSE;
        public static Supplier<Integer> ZOMBIE_HORSE_COST;
        public static Supplier<Boolean> ZOMBIE_HORSE_UNDERWATER;
        public static Supplier<Boolean> PLACEABLE_GUNPOWDER;
        public static Supplier<Integer> GUNPOWDER_BURN_SPEED;
        public static Supplier<Integer> GUNPOWDER_SPREAD_AGE;
        public static Supplier<Boolean> MIXED_BOOKS;
        public static Supplier<Boolean> SKULL_PILES;
        public static Supplier<Boolean> SKULL_CANDLES;
        public static Supplier<Boolean> SKULL_CANDLES_MULTIPLE;
        public static Supplier<Boolean> WANDERING_TRADER_DOORS;

        private static void init(ConfigBuilder builder) {
            builder.comment("Vanilla tweaks")
                    .push("tweaks");
            WANDERING_TRADER_DOORS = builder.comment("Allows traders to open doors (because they couldnt aparently)")
                    .define("traders_open_doors", true);

            builder.push("dispenser_tweaks");
            AXE_DISPENSER_BEHAVIORS = builder.comment("Allows dispensers to use axes on blocks to strip logs and scrape off copper oxidation and wax")
                    .define("axe_strip", true);
            ENDER_PEAR_DISPENSERS = builder.comment("Enables shooting ender pearls with dispensers")
                    .define("shoot_ender_pearls", true);
            builder.pop();


            //double cake
            builder.push("cake_tweaks");
            DOUBLE_CAKE_PLACEMENT = builder.comment("Allows you to place a cake on top of another")
                    .define("double_cake", true);
            DIRECTIONAL_CAKE = builder.comment("Allows eating a cake from every side")
                    .define("directional_cake", true);
            builder.pop();

            //skulls stuff
            builder.push("mob_head_tweaks");
            SKULL_PILES = builder.comment("Allows you to place two mob heads on top of each other")
                    .define("skull_piles", true);
            SKULL_CANDLES = builder.comment("Allows candles to be placed on top of skulls")
                    .define("skull_candles", true);
            SKULL_CANDLES_MULTIPLE = builder.comment("Allows placing more than one candle ontop of each skull")
                    .define("multiple_candles", true);
            builder.pop();

            //hanging pot
            builder.push("hanging_flower_pots");
            HANGING_POT_PLACEMENT = builder.comment("allows you to place hanging flower pots. Works with any modded pot too")
                    .define("enabled", true);
            builder.pop();

            //throwable bricks
            builder.push("throwable_bricks");
            THROWABLE_BRICKS_ENABLED = builder.comment("Throw bricks at your foes! Might break glass blocks")
                    .define("enabled", true);
            builder.pop();

            //wall lantern
            builder.push("lantern_tweaks");
            WALL_LANTERN_PLACEMENT = builder.comment("Allow wall lanterns placement")
                    .define("enabled", true);

            WALL_LANTERN_HIGH_PRIORITY = builder.comment("Gives high priority to wall lantern placement. Enable to override other wall lanterns placements, disable if it causes issues with other mods that use lower priority block click events")
                    .define("high_priority", true);

            List<String> modBlacklist = Arrays.asList("extlights", "betterendforge", "tconstruct", "enigmaticlegacy");
            WALL_LANTERN_BLACKLIST = builder.comment("Mod ids of mods that have lantern block that extend the base lantern class but don't look like one")
                    .define("mod_blacklist", modBlacklist);
            FALLING_LANTERNS = builder.comment("Allows ceiling lanterns to fall if their support is broken." +
                            "Additionally if they fall from high enough they will break creating a fire where they land")
                    .define("fallin_lanterns", LightableLanternBlock.FallMode.ON);
            builder.pop();
            //bells
            builder.push("bells_tweaks");
            BELL_CHAIN = builder.comment("Ring a bell by clicking on a chain that's connected to it")
                    .define("chain_ringing", true);
            BELL_CHAIN_LENGTH = builder.comment("Max chain length that allows a bell to ring")
                    .define("chain_length", 16, 0, 256);
            builder.pop();

            builder.push("placeable_sticks");
            PLACEABLE_STICKS = builder.comment("Allow placeable sticks")
                    .define("sticks", true);
            PLACEABLE_RODS = builder.comment("Allow placeable blaze rods")
                    .define("blaze_rods", true);
            builder.pop();

            builder.push("placeable_gunpowder");
            PLACEABLE_GUNPOWDER = builder.comment("Allow placeable gunpowder")
                    .define("enabled", true);
            GUNPOWDER_BURN_SPEED = builder.comment("Number of ticks it takes for gunpowder to burn 1 stage (out of 8). Increase to slow it down")
                    .define("speed", 2, 0, 20);
            GUNPOWDER_SPREAD_AGE = builder.comment("Age at which it spread to the next gunpowder block. Also affects speed")
                    .define("spread_age", 2, 0, 8);
            builder.pop();

            builder.push("raked_gravel");
            RAKED_GRAVEL = builder.comment("allow gravel to be raked with a hoe")
                    .define("enabled", true);
            builder.pop();

            builder.push("bottle_xp");
            BOTTLE_XP = builder.comment("Allow bottling up xp by using a bottle on an enchanting table")
                    .define("enabled", false);
            BOTTLING_COST = builder.comment("bottling health cost")
                    .define("cost", 2, 0, 20);
            builder.pop();

            builder.push("map_tweaks");
            CUSTOM_ADVENTURER_MAPS_TRADES = builder.comment("""
                            In this section you can add custom structure maps to cartographers
                            The format required is as follows:
                            [[<structure>,<level>,<min_price>,<max_price>,<map_name>,<map_color>,<map_marker>],[<structure>,...,<map_marker>],...]
                            With the following parameters:
                             - <structure> structure id to be located (ie: minecraft:igloo)
                             - <level> villager trading level at which the map will be sold. Must be between 1 and 5
                             - <min_price> minimum emerald price
                             - <max_price> maximum emerald price
                             - <map_name> map item name
                             - <map_color> hex color of the map item overlay texture
                             - <map_marker> id of the map marker to be used (ie: supplementaries:igloo).\s
                            See texture folder for all the names. Leave empty for default ones.
                            You can also use vanilla map markers by referring to them with their enum name (i.e: minecraft:target_x)
                            Other vanilla valid ones are: player, target, red_marker, target_point, player_off_map, player_off_limits, mansion, monument, red_x, banner_white
                            Note that ony the first parameter is required, each of the others others can me removed and will be defaulted to reasonable values
                            example: ['minecraft:swamp_hut','2','5','7','witch hut map','0x00ff33']""")

                    .defineForgeList("custom_adventurer_maps", Collections.singletonList(Collections.singletonList("")), LIST_STRING_CHECK);

            RANDOM_ADVENTURER_MAPS = builder.comment("Cartographers will sell 'adventurer maps' that will lead to a random vanilla structure (choosen from a thought out preset list).\n" +
                            "Best kept disabled if you are adding custom adventurer maps with its config")
                    .define("random_adventurer_maps", true);
            MAP_MARKERS = builder.comment("enables beacons, lodestones, respawn anchors, beds, conduits, portals to be displayed on maps by clicking one of them with a map")
                    .define("block_map_markers", true);
            builder.pop();

            builder.push("ceiling_banners");
            CEILING_BANNERS = builder.comment("Allow banners to be placed on ceilings")
                    .define("enabled", true);
            builder.pop();

            builder.push("placeable_books");
            WRITTEN_BOOKS = builder.comment("Allows written books to be placed down. Requires shift clicking")
                    .define("enabled", true);
            PLACEABLE_BOOKS = builder.comment("Allow books and enchanted books to be placed on the ground")
                    .define("enabled", true);
            BOOK_POWER = builder.comment("Enchantment power bonus given by normal book piles with 4 books. Piles with less books will have their respective fraction of this total. For reference a vanilla bookshelf provides 1")
                    .define("book_power", 1d, 0, 5);
            ENCHANTED_BOOK_POWER = builder.comment("Enchantment power bonus given by normal book piles with 4 books. Piles with less books will have their respective fraction of this total. For reference a vanilla bookshelf provides 1")
                    .define("enchanted_book_power", 1.334d, 0, 5);
            MIXED_BOOKS = builder.comment("Allow all books to be placed both vertically and horizontally")
                    .define("mixed_books", false);
            builder.pop();

            builder.push("zombie_horse");
            ZOMBIE_HORSE = builder.comment("Feed a stack of rotten flesh to a skeleton horse to buff him up to a zombie horse")
                    .define("zombie_horse_conversion", true);
            ZOMBIE_HORSE_COST = builder.comment("Amount of rotten flesh needed")
                    .define("rotten_flesh", 64, 1, 1000);
            ZOMBIE_HORSE_UNDERWATER = builder.comment("Allows zombie horses to be ridden underwater")
                    .define("rideable_underwater", true);
            builder.pop();


            builder.pop();
        }

    }

    public static class General {
        public static Supplier<Boolean> SERVER_PROTECTION;

        private static void init(ConfigBuilder builder) {
            builder.comment("General settings")
                    .push("general");
            SERVER_PROTECTION = builder.comment("Turn this on to disable any interaction on blocks placed by other players. This affects item shelves, signs, flower pots, and boards. " +
                            "Useful for protected servers. Note that it will affect only blocks placed after this is turned on and such blocks will keep being protected after this option is disabled")
                    .define("server_protection", false);

            builder.pop();
        }
    }

    public static class Blocks {

        public static Supplier<Boolean> BAMBOO_SPIKES_ALTERNATIVE;

        public static Supplier<Integer> BUBBLE_LIFETIME;
        public static Supplier<Boolean> BUBBLE_BREAK;
        public static Supplier<Boolean> BUBBLE_FEATHER_FALLING;

        public static Supplier<Boolean> ROPE_UNRESTRICTED;
        public static Supplier<Boolean> ROPE_SLIDE;
        public static Supplier<Integer> GLOBE_TRADES;

        public static Supplier<Integer> SPEAKER_RANGE;
        public static Supplier<Boolean> SPEAKER_NARRATOR;

        public static Supplier<Integer> BELLOWS_PERIOD;
        public static Supplier<Integer> BELLOWS_POWER_SCALING;
        public static Supplier<Double> BELLOWS_MAX_VEL;
        public static Supplier<Double> BELLOWS_BASE_VEL_SCALING;
        public static Supplier<Boolean> BELLOWS_FLAG;
        public static Supplier<Integer> BELLOWS_RANGE;

        public static Supplier<Double> LAUNCHER_VEL;
        public static Supplier<Integer> LAUNCHER_HEIGHT;

        public static Supplier<Boolean> TURN_TABLE_ROTATE_ENTITIES;

        public static Supplier<Integer> JAR_CAPACITY;
        public static Supplier<Boolean> JAR_EAT;
        public static Supplier<Boolean> JAR_CAPTURE;
        public static Supplier<Boolean> JAR_COOKIES;
        public static Supplier<Boolean> JAR_LIQUIDS;
        public static Supplier<Boolean> JAR_ITEM_DRINK;
        public static Supplier<Boolean> JAR_AUTO_DETECT;
        public static Supplier<Boolean> GOBLET_DRINK;
        public static Supplier<Boolean> CRYSTAL_ENCHANTING;

        public static Supplier<Boolean> CAGE_ALL_MOBS;
        public static Supplier<Boolean> CAGE_ALL_BABIES;
        public static Supplier<Boolean> CAGE_AUTO_DETECT;
        public static Supplier<Boolean> CAGE_PERSISTENT_MOBS;
        public static Supplier<Integer> CAGE_HEALTH_THRESHOLD;

        public static Supplier<Boolean> NOTICE_BOARDS_UNRESTRICTED;

        public static Supplier<Boolean> SACK_PENALTY;
        public static Supplier<Integer> SACK_INCREMENT;
        public static Supplier<Integer> SACK_SLOTS;

        public static Supplier<Boolean> SAFE_UNBREAKABLE;
        public static Supplier<Boolean> SAFE_SIMPLE;

        public static Supplier<Boolean> BLACKBOARD_COLOR;

        public static Supplier<Boolean> REPLACE_DAUB;
        public static Supplier<Boolean> SWAP_TIMBER_FRAME;

        public static Supplier<Integer> HOURGLASS_DUST;
        public static Supplier<Integer> HOURGLASS_SAND;
        public static Supplier<Integer> HOURGLASS_CONCRETE;
        public static Supplier<Integer> HOURGLASS_BLAZE_POWDER;
        public static Supplier<Integer> HOURGLASS_GLOWSTONE;
        public static Supplier<Integer> HOURGLASS_REDSTONE;
        public static Supplier<Integer> HOURGLASS_SUGAR;
        public static Supplier<Integer> HOURGLASS_SLIME;
        public static Supplier<Integer> HOURGLASS_HONEY;

        public static Supplier<Boolean> ITEM_SHELF_LADDER;

        public static Supplier<Boolean> DOUBLE_IRON_GATE;
        public static Supplier<Boolean> CONSISTENT_GATE;

        public static Supplier<Boolean> STICK_POLE;
        public static Supplier<Integer> STICK_POLE_LENGTH;

        public static Supplier<Boolean> ASH_BURN;
        public static Supplier<Boolean> ASH_RAIN;

        public static Supplier<Boolean> PLANTER_BREAKS;


        private static void init(ConfigBuilder builder) {

            builder.comment("Server side blocks configs")
                    .push("blocks");

            builder.push("bamboo_spikes");
            BAMBOO_SPIKES_ALTERNATIVE = builder.comment("Alternative mode for bamboo spikes. Allows only harmful effects to be applied on them and they obtain infinite durability")
                    .define("alternative_mode", true);
            builder.pop();

            builder.push("planter");
            PLANTER_BREAKS = builder.comment("Makes so saplings that grow in a planter will break it turning into rooted dirt")
                    .define("broken_by_sapling", true);
            builder.pop();

            builder.push("bubble_block");
            BUBBLE_LIFETIME = builder.comment("Max lifetime of bubble blocks. Set to 10000 to have it infinite")
                    .define("lifetime", 20 * 60, 1, 10000);
            BUBBLE_BREAK = builder.comment("Can bubble break when touched on?")
                    .define("break_when_touched", true);
            BUBBLE_FEATHER_FALLING = builder.comment("If true feather falling prevents breaking bubbles when stepping on them")
                    .define("feather_falling_prevents_breaking",true);
            builder.pop();

            builder.push("ash");
            ASH_BURN = builder.comment("Burnable blocks will have a chance to create ash layers when burned")
                    .define("ash_from_fire", true);
            ASH_RAIN = builder.comment("Allows rain to wash away ash layers overtime")
                    .define("rain_wash_ash", true);
            builder.pop();

            builder.push("rope");
            ROPE_UNRESTRICTED = builder.comment("Allows ropes to be supported & attached to solid block sides")
                    .define("block_side_attachment", false);
            ROPE_SLIDE = builder.comment("Makes sliding down ropes as fast as free falling, still negating fall damage")
                    .define("slide_on_fall", true);
            builder.pop();

            builder.push("pedestal");
            CRYSTAL_ENCHANTING = builder.comment("If enabled end crystals placed on a pedestals will provide an enchantment power bonus equivalent to 3 bookshelves")
                    .define("crystal_enchanting", true);
            builder.pop();

            //globe
            builder.push("globe");
            GLOBE_TRADES = builder.comment("how many globe trades to give to the wandering trader. This will effectively increase the chance of him having a globe trader. Increase this if you have other mods that add stuff to that trader")
                    .define("chance", 2, 0, 50);
            builder.pop();

            //speaker
            builder.push("speaker_block");
            SPEAKER_NARRATOR = builder.comment("Enable/disable speaker block narrator mode")
                    .define("narrator_enabled", true);
            SPEAKER_RANGE = builder.comment("Maximum block range")
                    .define("range", 64, 0, 100000000);
            builder.pop();
            //bellows
            builder.push("bellows");
            BELLOWS_PERIOD = builder.comment("""
                            bellows pushes air following this equation:\s
                            air=(sin(2PI*ticks/period)<0), with period = base_period-(redstone_power-1)*power_scaling\s
                            represents base period at 1 power""")
                    .define("base_period", 78, 1, 512);
            BELLOWS_POWER_SCALING = builder.comment("how much the period changes in relation to the block redstone power")
                    .define("power_scaling", 3, 0, 128);
            BELLOWS_BASE_VEL_SCALING = builder.comment("""
                            velocity increase uses this equation:\s
                            vel = base_vel*((range-entity_distance)/range) with base_vel = base_velocity_scaling/period\s
                            note that the block will push further the faster it's pulsing""")
                    .define("base_velocity_scaling", 5.0, 0.0, 64);
            BELLOWS_MAX_VEL = builder.comment("entities with velocity greater than this won't be pushed")
                    .define("power_scaling", 2.0, 0.0, 16);
            BELLOWS_FLAG = builder.comment("sets velocity changed flag when pushing entities +\n" +
                            "causes pushing animation to be smooth client side but also restricts player movement when being pushed")
                    .define("velocity_changed_flag", true);
            BELLOWS_RANGE = builder.comment("maximum range")
                    .comment("note that it will still only keep alive the two fire blocks closer to it")
                    .define("range", 5, 0, 16);
            builder.pop();
            //spring launcher
            builder.push("spring_launcher");
            LAUNCHER_VEL = builder.comment("spring launcher launch speed")
                    .define("velocity", 1.5D, 0, 16);
            LAUNCHER_HEIGHT = builder.comment("fall distance needed to trigger the automatic spring launch")
                    .define("fall_height_required", 5, 0, 512);
            builder.pop();
            //turn table
            builder.push("turn_table");
            TURN_TABLE_ROTATE_ENTITIES = builder.comment("can rotate entities standing on it?")
                    .define("rotate_entities", true);
            builder.pop();
            //jar
            builder.push("jar");
            JAR_CAPACITY = builder.comment("Jar liquid capacity: leave at 12 for pixel accuracy")
                    .define("capacity", 12, 0, 1024);
            JAR_EAT = builder.comment("Allow right click to instantly eat or drink food or potions inside a placed jar.\n" +
                            "Disable if you think this ability is op (honey for example). Cookies are excluded")
                    .define("drink_from_jar", false);
            JAR_ITEM_DRINK = builder.comment("Allows the player to directly drink from jar items")
                    .define("drink_from_jar_item", false);
            JAR_AUTO_DETECT = builder.comment("Dynamically allows all small mobs inside jars depending on their hitbox size. Tinted jars can accept hostile mbos too")
                    .define("jar_auto_detect", false);
            JAR_CAPTURE = builder.comment("Allow Jars to capture small mobs")
                    .define("jar_capture", true);
            JAR_COOKIES = builder.comment("Allow Jars to hold cookies")
                    .define("jar_cookies", true);
            JAR_LIQUIDS = builder.comment("Allow Jars to hold liquids from bottles, buckets and bowls")
                    .define("jar_liquids", true);

            builder.pop();

            //cage
            builder.push("cage");
            CAGE_ALL_MOBS = builder.comment("Allows all entities to be captured by cages and jars. Not meant for survival")
                    .define("allow_all_mobs", false);
            CAGE_ALL_BABIES = builder.comment("Allows all baby mobs to be captured by cages")
                    .define("cage_allow_all_babies", false);
            CAGE_AUTO_DETECT = builder.comment("Dynamically allows all small mobs inside cages depending on their hitbox size")
                    .define("cage_auto_detect", false);
            CAGE_PERSISTENT_MOBS = builder.comment("Makes it so all (hostile) mobs captured by cages and jars will be set to persistent so they won't despawn when released")
                    .define("persistent_mobs", false);
            CAGE_HEALTH_THRESHOLD = builder.comment("Health percentage under which mobs will be allowed to be captured by cages and jars. Leave at 100 to accept any health level")
                    .define("health_threshold", 100, 1, 100);
            builder.pop();

            builder.push("goblet");
            GOBLET_DRINK = builder.comment("Allows drinking from goblets").define("allow_drinking", true);
            builder.pop();

            //notice boards
            builder.push("notice_board");
            NOTICE_BOARDS_UNRESTRICTED = builder.comment("Allows notice boards to accept and display any item, not just maps and books")
                    .define("allow_any_item", false);
            builder.pop();

            builder.push("sack");
            SACK_PENALTY = builder.comment("Penalize the player with slowness effect when carrying too many sacks")
                    .define("sack_penalty", true);
            SACK_INCREMENT = builder.comment("Maximum number of sacks after which the overencumbered effect will be applied. Each multiple of this number will increase the effect strength by one")
                    .define("sack_increment", 2, 0, 50);
            SACK_SLOTS = builder.comment("How many slots should a sack have")
                    .define("slots", 9, 1, 27);
            builder.pop();

            builder.push("safe");
            SAFE_UNBREAKABLE = builder.comment("Makes safes only breakable by their owner or by a player in creative")
                    .define("prevent_breaking", false);
            SAFE_SIMPLE = builder.comment("Make safes simpler so they do not require keys:\n" +
                            "they will be bound to the first person that opens one and only that person will be able to interact with them")
                    .define("simple_safes", false);
            builder.pop();

            builder.push("blackboard");
            BLACKBOARD_COLOR = builder.comment("Enable to draw directly on a blackboard using any dye. Gui still only works in black and white")
                    .define("colored_blackboard", false);
            builder.pop();

            builder.push("timber_frame");
            REPLACE_DAUB = builder.comment("Replace a timber frame with wattle and daub block when daub is placed in it")
                    .define("replace_daub", true);
            SWAP_TIMBER_FRAME = builder.comment("Allow placing a timber frame directly on a block by holding shift")
                    .define("swap_on_shift", false);

            builder.pop();

            builder.push("hourglass");
            HOURGLASS_SUGAR = builder.comment("Time in ticks for sugar")
                    .define("sugar_time", 40, 0, 10000);
            HOURGLASS_SAND = builder.comment("Time in ticks for sand blocks")
                    .define("sand_time", 70, 0, 10000);
            HOURGLASS_CONCRETE = builder.comment("Time in ticks for concrete blocks")
                    .define("concrete_time", 105, 0, 10000);
            HOURGLASS_DUST = builder.comment("Time in ticks for generic dust")
                    .define("dust_time", 150, 0, 10000);
            HOURGLASS_GLOWSTONE = builder.comment("Time in ticks for glowstone dust")
                    .define("glowstone_time", 190, 0, 10000);
            HOURGLASS_BLAZE_POWDER = builder.comment("Time in ticks for blaze powder")
                    .define("blaze_powder_time", 277, 0, 10000);
            HOURGLASS_REDSTONE = builder.comment("Time in ticks for redstone dust")
                    .define("redstone_time", 400, 0, 10000);
            HOURGLASS_SLIME = builder.comment("Time in ticks for slime balls")
                    .define("slime_time", 1750, 0, 10000);
            HOURGLASS_HONEY = builder.comment("Time in ticks for honey")
                    .define("honey_time", 2000, 0, 10000);
            builder.pop();

            builder.push("item_shelf");
            ITEM_SHELF_LADDER = builder.comment("Makes item shelves climbable")
                    .define("climbable_shelves", false);
            builder.pop();

            builder.push("iron_gate");
            DOUBLE_IRON_GATE = builder.comment("Allows two iron gates to be opened simultaneously when on top of the other")
                    .define("double_opening", true);
            CONSISTENT_GATE = builder.comment("Makes iron (ang gold) gates behave like their door counterpart so for example iron gates will only be openeable by redstone")
                    .define("door-like_gates", false);
            builder.pop();

            builder.push("flag");
            STICK_POLE = builder.comment("Allows right/left clicking on a stick to lower/raise a flag attached to it")
                    .define("stick_pole", true);
            STICK_POLE_LENGTH = builder.comment("Maximum allowed pole length")
                    .define("pole_length", 16, 0, 256);
            builder.pop();

            builder.pop();

        }
    }

    public static class Spawns {
        public static Supplier<Boolean> DISTANCE_TEXT;
        public static Supplier<Boolean> WAY_SIGN_ENABLED;
        public static Supplier<Integer> ROAD_SIGN_DISTANCE_MIN;
        public static Supplier<Integer> ROAD_SIGN_DISTANCE_AVR;

        public static Supplier<Boolean> WILD_FLAX_ENABLED;
        public static Supplier<Integer> FLAX_PATCH_TRIES;
        public static Supplier<Integer> FLAX_AVERAGE_EVERY;

        public static Supplier<Boolean> URN_PILE_ENABLED;
        public static Supplier<Integer> URN_PATCH_TRIES;
        public static Supplier<Integer> URN_PER_CHUNK;
        public static Supplier<List<String>> URN_BIOME_BLACKLIST;


        private static void init(ConfigBuilder builder) {
            builder.comment("Configure spawning conditions")
                    .push("spawns");


            builder.push("way_sign");
            ROAD_SIGN_DISTANCE_AVR = builder.comment("Average distance apart in chunks between spawn attempts. Has to be larger than minimum_distance of course")
                    .define("average_distance", 19, 0, 1000);
            ROAD_SIGN_DISTANCE_MIN = builder.comment("Minimum distance apart in chunks between spawn attempts")
                    .define("minimum_distance", 10, 0, 1000);
            WAY_SIGN_ENABLED = builder.comment("Entirely disables them from spawning")
                    .define("enabled", true);
            DISTANCE_TEXT = builder.comment("With this option road signs will display the distance to the structure that they are pointing to")
                    .define("show_distance_text", true);

            builder.pop();

            builder.push("wild_flax");
            WILD_FLAX_ENABLED = builder.define("enabled", true);
            FLAX_AVERAGE_EVERY = builder.comment("Spawn wild flax on average every 'x' chunks. Increases spawn frequency")
                    .define("rarity", 6, 1, 100);
            FLAX_PATCH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 35, 1, 100);
            builder.pop();

            builder.push("cave_urns");
            URN_PILE_ENABLED = builder.define("enabled", true);
            URN_PATCH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 4, 1, 100);
            URN_PER_CHUNK = builder.comment("Spawn attempts per chunk. Increases spawn frequency")
                    .define("spawn_attempts", 7, 0, 100);
            List<String> urnBlacklist = List.of("minecraft:lush_caves", "minecraft:dripstone_caves");
            URN_BIOME_BLACKLIST = builder.comment("Biomes in which urns won't spawn")
                    .define("biome_blacklist", urnBlacklist);
            builder.pop();

            builder.pop();
        }
    }

}