package net.mehvahdjukaar.supplementaries.configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.mehvahdjukaar.moonlight.configs.ConfigHelper.LIST_STRING_CHECK;
import static net.mehvahdjukaar.moonlight.configs.ConfigHelper.STRING_CHECK;


public class ServerConfigs {


    //overwritten by server one
    public static ForgeConfigSpec SERVER_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        block.init(builder);
        spawn.init(builder);
        entity.init(builder);
        entity.init(builder);
        tweaks.init(builder);
        item.init(builder);
        general.init(builder);

        SERVER_SPEC = builder.build();
    }

    public static void loadLocal() {
        CommentedFileConfig replacementConfig = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(Supplementaries.MOD_ID + "-common.toml"))
                .sync()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        replacementConfig.load();
        ServerConfigs.SERVER_SPEC.setConfig(replacementConfig);
    }


    public static class item {
        public static ForgeConfigSpec.IntValue ROPE_ARROW_CAPACITY;
        public static ForgeConfigSpec.BooleanValue ROPE_ARROW_CROSSBOW;
        public static ForgeConfigSpec.ConfigValue<String> ROPE_ARROW_ROPE;
        public static ForgeConfigSpec.IntValue FLUTE_RADIUS;
        public static ForgeConfigSpec.IntValue FLUTE_DISTANCE;
        public static ForgeConfigSpec.DoubleValue BOMB_RADIUS;
        public static ForgeConfigSpec.IntValue BOMB_FUSE;
        public static ForgeConfigSpec.EnumValue<BombEntity.BreakingMode> BOMB_BREAKS;
        public static ForgeConfigSpec.DoubleValue BOMB_BLUE_RADIUS;
        public static ForgeConfigSpec.EnumValue<BombEntity.BreakingMode> BOMB_BLUE_BREAKS;
        public static ForgeConfigSpec.DoubleValue SLINGSHOT_RANGE;
        public static ForgeConfigSpec.IntValue SLINGSHOT_CHARGE;
        public static ForgeConfigSpec.DoubleValue SLINGSHOT_DECELERATION;
        public static ForgeConfigSpec.BooleanValue UNRESTRICTED_SLINGSHOT;
        public static ForgeConfigSpec.EnumValue<Hands> WRENCH_BYPASS;
        public static ForgeConfigSpec.IntValue BUBBLE_BLOWER_COST;

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.push("items");

            builder.push("bubble_blower");
            BUBBLE_BLOWER_COST = builder.comment("Amount of soap consumed per bubble block placed")
                    .defineInRange("stasis_cost", 5, 1, 25);
            builder.pop();

            builder.push("wrench");
            WRENCH_BYPASS = builder.comment("Allows wrenches to bypass a block interaction action prioritizing their own when on said hand")
                    .defineEnum("bypass_when_on", Hands.MAIN_HAND);
            builder.pop();

            //rope arrow
            builder.push("rope_arrow");
            ROPE_ARROW_ROPE = builder.comment("If you don't like my ropes you can specify here the block id of" +
                            "a rope from another mod which will get deployed by rope arrows instead of mine")
                    .define("rope_arrow_override", "supplementaries:rope");
            ROPE_ARROW_CAPACITY = builder.comment("Max number of robe items allowed to be stored inside a rope arrow")
                    .defineInRange("capacity", 32, 1, 256);
            ROPE_ARROW_CROSSBOW = builder.comment("Makes rope arrows exclusive to crossbows")
                    .define("exclusive_to_crossbows", false);
            builder.pop();
            //flute
            builder.push("flute");
            FLUTE_RADIUS = builder.comment("Radius in which an unbound flute will search pets")
                    .defineInRange("unbound_radius", 64, 0, 500);
            FLUTE_DISTANCE = builder.comment("Max distance at which a bound flute will allow a pet to teleport")
                    .defineInRange("bound_distance", 64, 0, 500);

            builder.pop();
            //bomb
            builder.push("bomb");
            BOMB_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .defineInRange("explosion_radius", 2, 0.1, 10);
            BOMB_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .defineEnum("break_blocks", BombEntity.BreakingMode.WEAK);
            BOMB_FUSE = builder.comment("Put here any number other than 0 to have your bombs explode after a certaom amount of ticks instead than on contact")
                    .defineInRange("bomb_fuse", 0, 0, 100000);
            builder.pop();

            builder.push("blue_bomb");
            BOMB_BLUE_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .defineInRange("explosion_radius", 5.15, 0.1, 10);
            BOMB_BLUE_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .defineEnum("break_blocks", BombEntity.BreakingMode.WEAK);

            builder.pop();

            builder.push("slingshot");
            SLINGSHOT_RANGE = builder.comment("Slingshot range multiplier. Affect the initial projectile speed")
                    .defineInRange("range_multiplier", 1f, 0, 5);
            SLINGSHOT_CHARGE = builder.comment("Time in ticks to fully charge a slingshot")
                    .defineInRange("charge_time", 20, 0, 100);
            SLINGSHOT_DECELERATION = builder.comment("Deceleration for the stasis projectile")
                    .defineInRange("stasis_deceleration", 0.9625, 0.1, 1);
            UNRESTRICTED_SLINGSHOT = builder.comment("Allow enderman to intercept any slingshot projectile")
                    .define("unrestricted_enderman_intercept", true);
            builder.pop();


            builder.pop();
        }

    }

    public enum Hands {
        MAIN_HAND, OFF_HAND, BOTH, NONE
    }

    public static class tweaks {
        public static ForgeConfigSpec.BooleanValue ENDER_PEAR_DISPENSERS;
        public static ForgeConfigSpec.BooleanValue AXE_DISPENSER_BEHAVIORS;
        public static ForgeConfigSpec.BooleanValue DIRECTIONAL_CAKE;
        public static ForgeConfigSpec.BooleanValue DOUBLE_CAKE_PLACEMENT;
        public static ForgeConfigSpec.BooleanValue HANGING_POT_PLACEMENT;
        public static ForgeConfigSpec.BooleanValue WALL_LANTERN_PLACEMENT;
        public static ForgeConfigSpec.BooleanValue WALL_LANTERN_HIGH_PRIORITY;
        public static ForgeConfigSpec.BooleanValue THROWABLE_BRICKS_ENABLED;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> WALL_LANTERN_BLACKLIST;
        public static ForgeConfigSpec.EnumValue<LightableLanternBlock.FallMode> FALLING_LANTERNS;
        public static ForgeConfigSpec.BooleanValue BELL_CHAIN;
        public static ForgeConfigSpec.IntValue BELL_CHAIN_LENGTH;
        public static ForgeConfigSpec.BooleanValue PLACEABLE_STICKS;
        public static ForgeConfigSpec.BooleanValue PLACEABLE_RODS;
        public static ForgeConfigSpec.BooleanValue RAKED_GRAVEL;
        public static ForgeConfigSpec.BooleanValue BOTTLE_XP;
        public static ForgeConfigSpec.IntValue BOTTLING_COST;
        public static ForgeConfigSpec.ConfigValue<List<? extends List<String>>> CUSTOM_ADVENTURER_MAPS_TRADES;
        public static ForgeConfigSpec.BooleanValue RANDOM_ADVENTURER_MAPS;
        public static ForgeConfigSpec.BooleanValue MAP_MARKERS;
        public static ForgeConfigSpec.BooleanValue CEILING_BANNERS;
        public static ForgeConfigSpec.BooleanValue PLACEABLE_BOOKS;
        public static ForgeConfigSpec.BooleanValue WRITTEN_BOOKS;
        public static ForgeConfigSpec.DoubleValue BOOK_POWER;
        public static ForgeConfigSpec.DoubleValue ENCHANTED_BOOK_POWER;
        public static ForgeConfigSpec.BooleanValue ZOMBIE_HORSE;
        public static ForgeConfigSpec.IntValue ZOMBIE_HORSE_COST;
        public static ForgeConfigSpec.BooleanValue ZOMBIE_HORSE_UNDERWATER;
        public static ForgeConfigSpec.BooleanValue PLACEABLE_GUNPOWDER;
        public static ForgeConfigSpec.IntValue GUNPOWDER_BURN_SPEED;
        public static ForgeConfigSpec.IntValue GUNPOWDER_SPREAD_AGE;
        public static ForgeConfigSpec.BooleanValue MIXED_BOOKS;
        public static ForgeConfigSpec.BooleanValue SKULL_PILES;
        public static ForgeConfigSpec.BooleanValue SKULL_CANDLES;
        public static ForgeConfigSpec.BooleanValue SKULL_CANDLES_MULTIPLE;

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Vanilla tweaks")
                    .push("tweaks");

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
                    .defineList("mod_blacklist", modBlacklist, STRING_CHECK);
            FALLING_LANTERNS = builder.comment("Allows ceiling lanterns to fall if their support is broken." +
                            "Additionally if they fall from high enough they will break creating a fire where they land")
                    .defineEnum("fallin_lanterns", LightableLanternBlock.FallMode.ON);
            builder.pop();
            //bells
            builder.push("bells_tweaks");
            BELL_CHAIN = builder.comment("Ring a bell by clicking on a chain that's connected to it")
                    .define("chain_ringing", true);
            BELL_CHAIN_LENGTH = builder.comment("Max chain length that allows a bell to ring")
                    .defineInRange("chain_length", 16, 0, 256);
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
                    .defineInRange("speed", 2, 0, 20);
            GUNPOWDER_SPREAD_AGE = builder.comment("Age at which it spread to the next gunpowder block. Also affects speed")
                    .defineInRange("spread_age", 2, 0, 8);
            builder.pop();

            builder.push("raked_gravel");
            RAKED_GRAVEL = builder.comment("allow gravel to be raked with a hoe")
                    .define("enabled", true);
            builder.pop();

            builder.push("bottle_xp");
            BOTTLE_XP = builder.comment("Allow bottling up xp by using a bottle on an enchanting table")
                    .define("enabled", false);
            BOTTLING_COST = builder.comment("bottling health cost")
                    .defineInRange("cost", 2, 0, 20);
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

                    .defineList("custom_adventurer_maps", Collections.singletonList(Collections.singletonList("")), LIST_STRING_CHECK);

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
                    .defineInRange("book_power", 1d, 0, 5);
            ENCHANTED_BOOK_POWER = builder.comment("Enchantment power bonus given by normal book piles with 4 books. Piles with less books will have their respective fraction of this total. For reference a vanilla bookshelf provides 1")
                    .defineInRange("enchanted_book_power", 1.334d, 0, 5);
            MIXED_BOOKS = builder.comment("Allow all books to be placed both vertically and horizontally")
                    .define("mixed_books", false);
            builder.pop();

            builder.push("zombie_horse");
            ZOMBIE_HORSE = builder.comment("Feed a stack of rotten flesh to a skeleton horse to buff him up to a zombie horse")
                    .define("zombie_horse_conversion", true);
            ZOMBIE_HORSE_COST = builder.comment("Amount of rotten flesh needed")
                    .defineInRange("rotten_flesh", 64, 1, 1000);
            ZOMBIE_HORSE_UNDERWATER = builder.comment("Allows zombie horses to be ridden underwater")
                    .define("rideable_underwater", true);
            builder.pop();


            builder.pop();
        }

    }

    public static class general {
        public static ForgeConfigSpec.BooleanValue SERVER_PROTECTION;

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("General settings")
                    .push("general");
            SERVER_PROTECTION = builder.comment("Turn this on to disable any interaction on blocks placed by other players. This affects item shelves, signs, flower pots, and boards. " +
                            "Useful for protected servers. Note that it will affect only blocks placed after this is turned on and such blocks will keep being protected after this option is disabled")
                    .define("server_protection", false);

            builder.pop();
        }
    }

    public static class block {

        public static ForgeConfigSpec.BooleanValue BAMBOO_SPIKES_ALTERNATIVE;

        public static ForgeConfigSpec.IntValue BUBBLE_LIFETIME;
        public static ForgeConfigSpec.BooleanValue BUBBLE_BREAK;

        public static ForgeConfigSpec.BooleanValue ROPE_UNRESTRICTED;
        public static ForgeConfigSpec.BooleanValue ROPE_SLIDE;
        public static ForgeConfigSpec.IntValue GLOBE_TRADES;

        public static ForgeConfigSpec.IntValue SPEAKER_RANGE;
        public static ForgeConfigSpec.BooleanValue SPEAKER_NARRATOR;

        public static ForgeConfigSpec.IntValue BELLOWS_PERIOD;
        public static ForgeConfigSpec.IntValue BELLOWS_POWER_SCALING;
        public static ForgeConfigSpec.DoubleValue BELLOWS_MAX_VEL;
        public static ForgeConfigSpec.DoubleValue BELLOWS_BASE_VEL_SCALING;
        public static ForgeConfigSpec.BooleanValue BELLOWS_FLAG;
        public static ForgeConfigSpec.IntValue BELLOWS_RANGE;

        public static ForgeConfigSpec.DoubleValue LAUNCHER_VEL;
        public static ForgeConfigSpec.IntValue LAUNCHER_HEIGHT;

        public static ForgeConfigSpec.BooleanValue TURN_TABLE_ROTATE_ENTITIES;

        public static ForgeConfigSpec.IntValue JAR_CAPACITY;
        public static ForgeConfigSpec.BooleanValue JAR_EAT;
        public static ForgeConfigSpec.BooleanValue JAR_CAPTURE;
        public static ForgeConfigSpec.BooleanValue JAR_COOKIES;
        public static ForgeConfigSpec.BooleanValue JAR_LIQUIDS;
        public static ForgeConfigSpec.BooleanValue JAR_ITEM_DRINK;
        public static ForgeConfigSpec.BooleanValue JAR_AUTO_DETECT;
        public static ForgeConfigSpec.BooleanValue GOBLET_DRINK;
        public static ForgeConfigSpec.BooleanValue CRYSTAL_ENCHANTING;

        public static ForgeConfigSpec.BooleanValue CAGE_ALL_MOBS;
        public static ForgeConfigSpec.BooleanValue CAGE_ALL_BABIES;
        public static ForgeConfigSpec.BooleanValue CAGE_AUTO_DETECT;
        public static ForgeConfigSpec.BooleanValue CAGE_PERSISTENT_MOBS;
        public static ForgeConfigSpec.IntValue CAGE_HEALTH_THRESHOLD;

        public static ForgeConfigSpec.BooleanValue NOTICE_BOARDS_UNRESTRICTED;

        public static ForgeConfigSpec.BooleanValue SACK_PENALTY;
        public static ForgeConfigSpec.IntValue SACK_INCREMENT;
        public static ForgeConfigSpec.IntValue SACK_SLOTS;

        public static ForgeConfigSpec.BooleanValue SAFE_UNBREAKABLE;
        public static ForgeConfigSpec.BooleanValue SAFE_SIMPLE;

        public static ForgeConfigSpec.BooleanValue BLACKBOARD_COLOR;

        public static ForgeConfigSpec.BooleanValue REPLACE_DAUB;
        public static ForgeConfigSpec.BooleanValue SWAP_TIMBER_FRAME;

        public static ForgeConfigSpec.IntValue HOURGLASS_DUST;
        public static ForgeConfigSpec.IntValue HOURGLASS_SAND;
        public static ForgeConfigSpec.IntValue HOURGLASS_CONCRETE;
        public static ForgeConfigSpec.IntValue HOURGLASS_BLAZE_POWDER;
        public static ForgeConfigSpec.IntValue HOURGLASS_GLOWSTONE;
        public static ForgeConfigSpec.IntValue HOURGLASS_REDSTONE;
        public static ForgeConfigSpec.IntValue HOURGLASS_SUGAR;
        public static ForgeConfigSpec.IntValue HOURGLASS_SLIME;
        public static ForgeConfigSpec.IntValue HOURGLASS_HONEY;

        public static ForgeConfigSpec.BooleanValue ITEM_SHELF_LADDER;

        public static ForgeConfigSpec.BooleanValue DOUBLE_IRON_GATE;
        public static ForgeConfigSpec.BooleanValue CONSISTENT_GATE;

        public static ForgeConfigSpec.BooleanValue STICK_POLE;
        public static ForgeConfigSpec.IntValue STICK_POLE_LENGTH;

        public static ForgeConfigSpec.BooleanValue ASH_BURN;
        public static ForgeConfigSpec.BooleanValue ASH_RAIN;


        private static void init(ForgeConfigSpec.Builder builder) {

            builder.comment("Server side blocks configs")
                    .push("blocks");

            builder.push("bamboo_spikes");
            BAMBOO_SPIKES_ALTERNATIVE = builder.comment("Alternative mode for bamboo spikes. Allows only harmful effects to be applied on them and they obtain infinite durability")
                    .define("alternative_mode", true);
            builder.pop();

            builder.push("bubble_block");
            BUBBLE_LIFETIME = builder.comment("Max lifetime of bubble blocks. Set to 10000 to have it infinite")
                    .defineInRange("lifetime", 20 * 60, 1, 10000);
            BUBBLE_BREAK = builder.comment("Can bubble break when stepped on?")
                    .define("break_when_touched", true);
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
                    .defineInRange("chance", 2, 0, 50);
            builder.pop();

            //speaker
            builder.push("speaker_block");
            SPEAKER_NARRATOR = builder.comment("Enable/disable speaker block narrator mode")
                    .define("narrator_enabled", true);
            SPEAKER_RANGE = builder.comment("Maximum block range")
                    .defineInRange("range", 64, 0, 100000000);
            builder.pop();
            //bellows
            builder.push("bellows");
            BELLOWS_PERIOD = builder.comment("""
                            bellows pushes air following this equation:\s
                            air=(sin(2PI*ticks/period)<0), with period = base_period-(redstone_power-1)*power_scaling\s
                            represents base period at 1 power""")
                    .defineInRange("base_period", 78, 1, 512);
            BELLOWS_POWER_SCALING = builder.comment("how much the period changes in relation to the block redstone power")
                    .defineInRange("power_scaling", 3, 0, 128);
            BELLOWS_BASE_VEL_SCALING = builder.comment("""
                            velocity increase uses this equation:\s
                            vel = base_vel*((range-entity_distance)/range) with base_vel = base_velocity_scaling/period\s
                            note that the block will push further the faster it's pulsing""")
                    .defineInRange("base_velocity_scaling", 5.0, 0.0, 64);
            BELLOWS_MAX_VEL = builder.comment("entities with velocity greater than this won't be pushed")
                    .defineInRange("power_scaling", 2.0, 0.0, 16);
            BELLOWS_FLAG = builder.comment("sets velocity changed flag when pushing entities +\n" +
                            "causes pushing animation to be smooth client side but also restricts player movement when being pushed")
                    .define("velocity_changed_flag", true);
            BELLOWS_RANGE = builder.comment("maximum range")
                    .comment("note that it will still only keep alive the two fire blocks closer to it")
                    .defineInRange("range", 5, 0, 16);
            builder.pop();
            //spring launcher
            builder.push("spring_launcher");
            LAUNCHER_VEL = builder.comment("spring launcher launch speed")
                    .defineInRange("velocity", 1.5D, 0, 16);
            LAUNCHER_HEIGHT = builder.comment("fall distance needed to trigger the automatic spring launch")
                    .defineInRange("fall_height_required", 5, 0, 512);
            builder.pop();
            //turn table
            builder.push("turn_table");
            TURN_TABLE_ROTATE_ENTITIES = builder.comment("can rotate entities standing on it?")
                    .define("rotate_entities", true);
            builder.pop();
            //jar
            builder.push("jar");
            JAR_CAPACITY = builder.comment("Jar liquid capacity: leave at 12 for pixel accuracy")
                    .defineInRange("capacity", 12, 0, 1024);
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
                    .defineInRange("health_threshold", 100, 1, 100);
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
                    .defineInRange("sack_increment", 2, 0, 50);
            SACK_SLOTS = builder.comment("How many slots should a sack have")
                    .defineInRange("slots", 9, 1, 27);
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
                    .defineInRange("sugar_time", 40, 0, 10000);
            HOURGLASS_SAND = builder.comment("Time in ticks for sand blocks")
                    .defineInRange("sand_time", 70, 0, 10000);
            HOURGLASS_CONCRETE = builder.comment("Time in ticks for concrete blocks")
                    .defineInRange("concrete_time", 105, 0, 10000);
            HOURGLASS_DUST = builder.comment("Time in ticks for generic dust")
                    .defineInRange("dust_time", 150, 0, 10000);
            HOURGLASS_GLOWSTONE = builder.comment("Time in ticks for glowstone dust")
                    .defineInRange("glowstone_time", 190, 0, 10000);
            HOURGLASS_BLAZE_POWDER = builder.comment("Time in ticks for blaze powder")
                    .defineInRange("blaze_powder_time", 277, 0, 10000);
            HOURGLASS_REDSTONE = builder.comment("Time in ticks for redstone dust")
                    .defineInRange("redstone_time", 400, 0, 10000);
            HOURGLASS_SLIME = builder.comment("Time in ticks for slime balls")
                    .defineInRange("slime_time", 1750, 0, 10000);
            HOURGLASS_HONEY = builder.comment("Time in ticks for honey")
                    .defineInRange("honey_time", 2000, 0, 10000);
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
                    .defineInRange("pole_length", 16, 0, 256);
            builder.pop();

            builder.pop();

        }
    }

    public static class spawn {
        public static ForgeConfigSpec.IntValue FIREFLY_MIN;
        public static ForgeConfigSpec.IntValue FIREFLY_MAX;
        public static ForgeConfigSpec.IntValue FIREFLY_WEIGHT;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> FIREFLY_BIOMES;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> FIREFLY_MOD_WHITELIST;

        public static ForgeConfigSpec.BooleanValue DISTANCE_TEXT;
        public static ForgeConfigSpec.BooleanValue WAY_SIGN_ENABLED;
        public static ForgeConfigSpec.IntValue ROAD_SIGN_DISTANCE_MIN;
        public static ForgeConfigSpec.IntValue ROAD_SIGN_DISTANCE_AVR;

        public static ForgeConfigSpec.BooleanValue WILD_FLAX_ENABLED;
        public static ForgeConfigSpec.IntValue FLAX_PATCH_TRIES;
        public static ForgeConfigSpec.IntValue FLAX_AVERAGE_EVERY;

        public static ForgeConfigSpec.BooleanValue URN_PILE_ENABLED;
        public static ForgeConfigSpec.IntValue URN_PATCH_TRIES;
        public static ForgeConfigSpec.IntValue URN_PER_CHUNK;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> URN_BIOME_BLACKLIST;


        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Configure spawning conditions")
                    .push("spawns");
            builder.push("entities");
            builder.push("firefly");
            List<String> defaultBiomes = Arrays.asList("minecraft:swamp", "minecraft:swamp_hills", "minecraft:plains",
                    "minecraft:sunflower_plains", "minecraft:dark_forest", "minecraft:dark_forest_hills", "byg:bayou",
                    "byg:cypress_swamplands", "byg:glowshroom_bayou", "byg:mangrove_marshes", "byg:vibrant_swamplands",
                    "byg:fresh_water_lake", "byg:grassland_plateau", "byg:wooded_grassland_plateau", "byg:flowering_grove",
                    "byg:guiana_shield", "byg:guiana_clearing", "byg:meadow", "byg:orchard", "byg:seasonal_birch_forest",
                    "byg:seasonal_deciduous_forest", "byg:seasonal_forest", "biomesoplenty:flower_meadow", "biomesoplenty:fir_clearing",
                    "biomesoplenty:grove_lakes", "biomesoplenty:grove", "biomesoplenty:highland_moor", "biomesoplenty:wetland_marsh",
                    "biomesoplenty:deep_bayou", "biomesoplenty:wetland");
            List<String> fireflyModWhitelist = List.of();

            FIREFLY_BIOMES = builder.comment("Spawnable biomes")
                    .defineList("biomes", defaultBiomes, STRING_CHECK);
            FIREFLY_MOD_WHITELIST = builder.comment("Whitelisted mods. All biomes from said mods will be able to spawn fireflies. Use the one above for more control")
                    .defineList("mod_whitelist", fireflyModWhitelist, STRING_CHECK);
            FIREFLY_WEIGHT = builder.comment("Spawn weight \n" +
                            "Set to 0 to disable spawning entirely")
                    .defineInRange("weight", 3, 0, 100);
            FIREFLY_MIN = builder.comment("Minimum group size")
                    .defineInRange("min", 5, 0, 64);
            FIREFLY_MAX = builder.comment("Maximum group size")
                    .defineInRange("max", 9, 0, 64);

            builder.pop();

            builder.push("owl");
            builder.pop();
            builder.pop();

            builder.push("structures");
            builder.push("way_sign");
            ROAD_SIGN_DISTANCE_AVR = builder.comment("Average distance apart in chunks between spawn attempts. Has to be larger than minimum_distance of course")
                    .defineInRange("average_distance", 19, 0, 1000);
            ROAD_SIGN_DISTANCE_MIN = builder.comment("Minimum distance apart in chunks between spawn attempts")
                    .defineInRange("minimum_distance", 10, 0, 1000);
            WAY_SIGN_ENABLED = builder.comment("Entirely disables them from spawning")
                    .define("enabled", true);
            DISTANCE_TEXT = builder.comment("With this option road signs will display the distance to the structure that they are pointing to")
                    .define("show_distance_text", true);

            builder.pop();

            builder.push("wild_flax");
            WILD_FLAX_ENABLED = builder.define("enabled", true);
            FLAX_AVERAGE_EVERY = builder.comment("Spawn wild flax on average every 'x' chunks. Increases spawn frequency")
                    .defineInRange("rarity", 6, 1, 100);
            FLAX_PATCH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .defineInRange("attempts_per_patch", 35, 1, 100);
            builder.pop();

            builder.push("cave_urns");
            URN_PILE_ENABLED = builder.define("enabled", true);
            URN_PATCH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .defineInRange("attempts_per_patch", 4, 1, 100);
            URN_PER_CHUNK = builder.comment("Spawn attempts per chunk. Increases spawn frequency")
                    .defineInRange("spawn_attempts", 7, 0, 100);
            List<String> urnBlacklist = List.of("minecraft:lush_caves", "minecraft:dripstone_caves");
            URN_BIOME_BLACKLIST = builder.comment("Biomes in which urns won't spawn")
                    .defineList("biome_blacklist", urnBlacklist, STRING_CHECK);
            builder.pop();

            builder.pop();

            builder.pop();
        }
    }

    public static class entity {
        public static ForgeConfigSpec.IntValue FIREFLY_PERIOD;
        public static ForgeConfigSpec.DoubleValue FIREFLY_SPEED;
        public static ForgeConfigSpec.BooleanValue FIREFLY_DESPAWN;

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("entities parameters")
                    .push("entities");
            builder.push("firefly");
            FIREFLY_PERIOD = builder.comment("""
                            firefly animation period
                            note that actual period will be this + a random number between 0 and 10
                            this needs to be here to allow correct despawning of the entity when it's not glowing
                            check client configs come more animation settings""")
                    .defineInRange("period", 65, 1, 200);
            FIREFLY_SPEED = builder.comment("firefly flying speed")
                    .defineInRange("speed", 0.3, 0, 10);
            FIREFLY_DESPAWN = builder.comment("despawn during the day")
                    .define("despawn", true);
            builder.pop();

            builder.pop();
        }
    }


    //TODO: yeet these
    //maybe not need but hey
    public static class cached {


        //items
        public static String ROPE_ARROW_ROPE;
        public static Holder.Reference<Block> ROPE_ARROW_BLOCK;
        public static int ROPE_ARROW_CAPACITY;
        public static boolean ROPE_ARROW_CROSSBOW;
        public static int FLUTE_RADIUS;
        public static int FLUTE_DISTANCE;
        public static float BOMB_RADIUS;
        public static int BOMB_FUSE;
        public static BombEntity.BreakingMode BOMB_BREAKS;
        public static float BOMB_BLUE_RADIUS;
        public static BombEntity.BreakingMode BOMB_BLUE_BREAKS;
        public static double SLINGSHOT_RANGE;
        public static int SLINGSHOT_CHARGE;
        public static float SLINGSHOT_DECELERATION;
        public static boolean UNRESTRICTED_SLINGSHOT;
        public static Hands WRENCH_BYPASS;
        public static int BUBBLE_BLOWER_COST;
        //tweaks
        public static int ZOMBIE_HORSE_COST;
        public static boolean ZOMBIE_HORSE;
        public static boolean ZOMBIE_HORSE_UNDERWATER;
        public static boolean DIRECTIONAL_CAKE;
        public static boolean DOUBLE_CAKE_PLACEMENT;
        public static boolean HANGING_POT_PLACEMENT;
        public static boolean THROWABLE_BRICKS_ENABLED;
        public static boolean WALL_LANTERN_PLACEMENT;
        public static boolean WALL_LANTERN_HIGH_PRIORITY;
        public static List<? extends String> WALL_LANTERN_BLACKLIST;
        public static LightableLanternBlock.FallMode FALLING_LANTERNS;
        public static boolean BELL_CHAIN;
        public static int BELL_CHAIN_LENGTH;
        public static boolean PLACEABLE_STICKS;
        public static boolean PLACEABLE_RODS;
        public static boolean RAKED_GRAVEL;
        public static boolean BOTTLE_XP;
        public static int BOTTLING_COST;
        public static boolean MAP_MARKERS;
        public static boolean CEILING_BANNERS;
        public static boolean PLACEABLE_BOOKS;
        public static boolean WRITTEN_BOOKS;
        public static float ENCHANTED_BOOK_POWER;
        public static float BOOK_POWER;
        public static boolean MIXED_BOOKS;
        public static boolean PLACEABLE_GUNPOWDER;
        public static int GUNPOWDER_BURN_SPEED;
        public static int GUNPOWDER_SPREAD_AGE;
        public static boolean SKULL_PILES;
        public static boolean SKULL_CANDLES;
        public static boolean SKULL_CANDLES_MULTIPLE;
        //spawns
        public static int FIREFLY_MIN;
        public static int FIREFLY_MAX;
        public static int FIREFLY_WEIGHT;
        public static List<? extends String> FIREFLY_BIOMES;
        public static List<? extends String> FIREFLY_MOD_WHITELIST;
        public static boolean FIREFLY_DESPAWN;
        public static boolean DISTANCE_TEXT;
        //blocks
        public static int BUBBLE_LIFETIME;
        public static boolean BUBBLE_BREAK;
        public static int SPEAKER_RANGE;
        public static boolean SPEAKER_NARRATOR;
        public static boolean ASH_BURN;
        public static boolean ASH_RAIN;
        public static int BELLOWS_BASE_PERIOD;
        public static int BELLOWS_POWER_SCALING;
        public static double BELLOWS_MAX_VEL;
        public static double BELLOWS_BASE_VEL_SCALING;
        public static boolean BELLOWS_FLAG;
        public static int BELLOWS_RANGE;
        public static double LAUNCHER_VEL;
        public static int LAUNCHER_HEIGHT;
        public static boolean TURN_TABLE_ROTATE_ENTITIES;
        public static int JAR_CAPACITY;
        public static boolean JAR_EAT;
        public static boolean JAR_CAPTURE;
        public static boolean JAR_COOKIES;
        public static boolean JAR_LIQUIDS;
        public static boolean JAR_ITEM_DRINK;
        public static boolean JAR_AUTO_DETECT;
        public static boolean NOTICE_BOARDS_UNRESTRICTED;
        public static boolean CAGE_ALL_MOBS;
        public static boolean CAGE_ALL_BABIES;
        public static boolean CAGE_AUTO_DETECT;
        public static int SACK_INCREMENT;
        public static boolean SACK_PENALTY;
        public static int SACK_SLOTS;
        public static boolean SAFE_UNBREAKABLE;
        public static boolean SAFE_SIMPLE;
        public static int GLOBE_TRADES;
        public static boolean BLACKBOARD_COLOR;
        public static boolean REPLACE_DAUB;
        public static boolean SWAP_TIMBER_FRAME;
        public static boolean ITEM_SHELF_LADDER;
        public static boolean DOUBLE_IRON_GATE;
        public static boolean CONSISTENT_GATE;
        public static boolean STICK_POLE;
        public static int STICK_POLE_LENGTH;
        public static boolean GOBLET_DRINK;
        public static boolean CAGE_PERSISTENT_MOBS;
        public static int CAGE_HEALTH_THRESHOLD;
        public static boolean CRYSTAL_ENCHANTING;
        public static boolean ROPE_UNRESTRICTED;
        public static boolean ROPE_SLIDE;
        public static boolean BAMBOO_SPIKES_ALTERNATIVE;


        public static boolean SERVER_PROTECTION;

        //entity
        public static int FIREFLY_PERIOD;
        public static double FIREFLY_SPEED;

        public static void refresh() {


            ZOMBIE_HORSE_COST = tweaks.ZOMBIE_HORSE_COST.get();
            ZOMBIE_HORSE = tweaks.ZOMBIE_HORSE.get();
            ZOMBIE_HORSE_UNDERWATER = tweaks.ZOMBIE_HORSE_UNDERWATER.get();
            BOTTLING_COST = tweaks.BOTTLING_COST.get();
            BOTTLE_XP = tweaks.BOTTLE_XP.get();
            RAKED_GRAVEL = tweaks.RAKED_GRAVEL.get() && RegistryConfigs.Reg.RAKED_GRAVEL_ENABLED.get();
            PLACEABLE_RODS = tweaks.PLACEABLE_RODS.get();
            PLACEABLE_STICKS = tweaks.PLACEABLE_STICKS.get();
            DIRECTIONAL_CAKE = tweaks.DIRECTIONAL_CAKE.get();
            DOUBLE_CAKE_PLACEMENT = tweaks.DOUBLE_CAKE_PLACEMENT.get();
            HANGING_POT_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            WALL_LANTERN_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            WALL_LANTERN_HIGH_PRIORITY = tweaks.WALL_LANTERN_HIGH_PRIORITY.get();
            WALL_LANTERN_BLACKLIST = tweaks.WALL_LANTERN_BLACKLIST.get();
            FALLING_LANTERNS = tweaks.FALLING_LANTERNS.get();
            THROWABLE_BRICKS_ENABLED = tweaks.THROWABLE_BRICKS_ENABLED.get();
            BELL_CHAIN = tweaks.BELL_CHAIN.get();
            BELL_CHAIN_LENGTH = tweaks.BELL_CHAIN_LENGTH.get();
            MAP_MARKERS = tweaks.MAP_MARKERS.get();
            CEILING_BANNERS = tweaks.CEILING_BANNERS.get();
            PLACEABLE_BOOKS = tweaks.PLACEABLE_BOOKS.get();
            WRITTEN_BOOKS = tweaks.WRITTEN_BOOKS.get();
            MIXED_BOOKS = tweaks.MIXED_BOOKS.get();
            BOOK_POWER = (float) ((double) tweaks.BOOK_POWER.get());
            ENCHANTED_BOOK_POWER = (float) ((double) tweaks.ENCHANTED_BOOK_POWER.get());
            PLACEABLE_GUNPOWDER = tweaks.PLACEABLE_GUNPOWDER.get();
            GUNPOWDER_BURN_SPEED = tweaks.GUNPOWDER_BURN_SPEED.get();
            GUNPOWDER_SPREAD_AGE = tweaks.GUNPOWDER_SPREAD_AGE.get();
            SKULL_PILES = tweaks.SKULL_PILES.get();
            SKULL_CANDLES = tweaks.SKULL_CANDLES.get();
            SKULL_CANDLES_MULTIPLE = tweaks.SKULL_CANDLES_MULTIPLE.get();

            BUBBLE_BLOWER_COST = item.BUBBLE_BLOWER_COST.get();
            WRENCH_BYPASS = item.WRENCH_BYPASS.get();
            ROPE_ARROW_CAPACITY = item.ROPE_ARROW_CAPACITY.get();
            ROPE_ARROW_CROSSBOW = item.ROPE_ARROW_CROSSBOW.get();
            ROPE_ARROW_ROPE = item.ROPE_ARROW_ROPE.get();
            ROPE_ARROW_BLOCK = (Holder.Reference<Block>) ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(ROPE_ARROW_ROPE))
                    .orElse(ForgeRegistries.BLOCKS.getHolder(ModRegistry.ROPE.get()).get());
            FLUTE_DISTANCE = item.FLUTE_DISTANCE.get();
            FLUTE_RADIUS = item.FLUTE_RADIUS.get();
            BOMB_BREAKS = item.BOMB_BREAKS.get();
            BOMB_RADIUS = (float) (item.BOMB_RADIUS.get() + 0f);
            BOMB_FUSE = item.BOMB_FUSE.get();
            BOMB_BLUE_BREAKS = item.BOMB_BLUE_BREAKS.get();
            BOMB_BLUE_RADIUS = (float) (item.BOMB_BLUE_RADIUS.get() + 0f);
            SLINGSHOT_RANGE = item.SLINGSHOT_RANGE.get();
            SLINGSHOT_CHARGE = item.SLINGSHOT_CHARGE.get();
            SLINGSHOT_DECELERATION = (float) (0f + item.SLINGSHOT_DECELERATION.get());
            UNRESTRICTED_SLINGSHOT = item.UNRESTRICTED_SLINGSHOT.get();

            FIREFLY_MIN = spawn.FIREFLY_MIN.get();
            FIREFLY_MAX = spawn.FIREFLY_MAX.get();
            FIREFLY_WEIGHT = spawn.FIREFLY_WEIGHT.get();
            FIREFLY_BIOMES = spawn.FIREFLY_BIOMES.get();
            FIREFLY_MOD_WHITELIST = spawn.FIREFLY_MOD_WHITELIST.get();

            DISTANCE_TEXT = spawn.DISTANCE_TEXT.get();

            GLOBE_TRADES = block.GLOBE_TRADES.get();

            SPEAKER_RANGE = block.SPEAKER_RANGE.get();
            SPEAKER_NARRATOR = block.SPEAKER_NARRATOR.get();

            BELLOWS_BASE_PERIOD = block.BELLOWS_PERIOD.get();
            BELLOWS_POWER_SCALING = block.BELLOWS_POWER_SCALING.get();
            BELLOWS_MAX_VEL = block.BELLOWS_MAX_VEL.get();
            BELLOWS_BASE_VEL_SCALING = block.BELLOWS_BASE_VEL_SCALING.get();
            BELLOWS_FLAG = block.BELLOWS_FLAG.get();
            BELLOWS_RANGE = block.BELLOWS_RANGE.get();

            LAUNCHER_VEL = block.LAUNCHER_VEL.get();
            LAUNCHER_HEIGHT = block.LAUNCHER_HEIGHT.get();

            TURN_TABLE_ROTATE_ENTITIES = block.TURN_TABLE_ROTATE_ENTITIES.get();

            JAR_CAPACITY = block.JAR_CAPACITY.get();
            JAR_EAT = block.JAR_EAT.get();
            JAR_CAPTURE = block.JAR_CAPTURE.get();
            JAR_COOKIES = block.JAR_COOKIES.get();
            JAR_LIQUIDS = block.JAR_LIQUIDS.get();
            JAR_ITEM_DRINK = block.JAR_ITEM_DRINK.get();
            JAR_AUTO_DETECT = block.JAR_AUTO_DETECT.get();

            NOTICE_BOARDS_UNRESTRICTED = block.NOTICE_BOARDS_UNRESTRICTED.get();

            CAGE_ALL_MOBS = block.CAGE_ALL_MOBS.get();
            CAGE_ALL_BABIES = block.CAGE_ALL_BABIES.get();
            CAGE_AUTO_DETECT = block.CAGE_AUTO_DETECT.get();
            CAGE_PERSISTENT_MOBS = block.CAGE_PERSISTENT_MOBS.get();
            CAGE_HEALTH_THRESHOLD = block.CAGE_HEALTH_THRESHOLD.get();

            SACK_INCREMENT = block.SACK_INCREMENT.get();
            SACK_PENALTY = block.SACK_PENALTY.get();
            SACK_SLOTS = block.SACK_SLOTS.get();

            SAFE_UNBREAKABLE = block.SAFE_UNBREAKABLE.get();
            SAFE_SIMPLE = block.SAFE_SIMPLE.get();

            BLACKBOARD_COLOR = block.BLACKBOARD_COLOR.get();

            REPLACE_DAUB = block.REPLACE_DAUB.get();
            SWAP_TIMBER_FRAME = block.SWAP_TIMBER_FRAME.get();

            ITEM_SHELF_LADDER = block.ITEM_SHELF_LADDER.get();

            DOUBLE_IRON_GATE = block.DOUBLE_IRON_GATE.get();
            CONSISTENT_GATE = block.CONSISTENT_GATE.get();

            STICK_POLE = block.STICK_POLE.get();
            STICK_POLE_LENGTH = block.STICK_POLE_LENGTH.get();

            GOBLET_DRINK = block.GOBLET_DRINK.get();
            CRYSTAL_ENCHANTING = block.CRYSTAL_ENCHANTING.get();
            ROPE_UNRESTRICTED = block.ROPE_UNRESTRICTED.get();
            ROPE_SLIDE = block.ROPE_SLIDE.get();
            ASH_BURN = block.ASH_BURN.get();
            ASH_RAIN = block.ASH_RAIN.get();
            BUBBLE_LIFETIME = block.BUBBLE_LIFETIME.get();
            BUBBLE_BREAK = block.BUBBLE_BREAK.get();
            BAMBOO_SPIKES_ALTERNATIVE = block.BAMBOO_SPIKES_ALTERNATIVE.get();

            FIREFLY_PERIOD = entity.FIREFLY_PERIOD.get();
            FIREFLY_SPEED = entity.FIREFLY_SPEED.get();
            FIREFLY_DESPAWN = entity.FIREFLY_DESPAWN.get();

            SERVER_PROTECTION = general.SERVER_PROTECTION.get();
        }
    }
}