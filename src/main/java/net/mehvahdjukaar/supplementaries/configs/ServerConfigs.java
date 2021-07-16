package net.mehvahdjukaar.supplementaries.configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ServerConfigs {


    //overwritten by server one
    public static ForgeConfigSpec SERVER_CONFIG;

    static {
        createConfig();
    }


    public static void createConfig(){
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        block.init(builder);
        spawn.init(builder);
        entity.init(builder);
        tweaks.init(builder);
        item.init(builder);

        SERVER_CONFIG = builder.build();
    }

    public static void loadLocal(){
        CommentedFileConfig replacementConfig = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(Supplementaries.MOD_ID + "-common.toml"))
                .sync()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        replacementConfig.load();
        ServerConfigs.SERVER_CONFIG.setConfig(replacementConfig);
    }


    public static class item {
        public static ForgeConfigSpec.ConfigValue<String> ROPE_ARROW_ROPE;
        public static ForgeConfigSpec.IntValue FLUTE_RADIUS;
        public static ForgeConfigSpec.IntValue FLUTE_DISTANCE;
        public static ForgeConfigSpec.DoubleValue BOMB_RADIUS;
        public static ForgeConfigSpec.BooleanValue BOMB_BREAKS;

        private static void init(ForgeConfigSpec.Builder builder){
            builder.push("items");

            //rope arrow
            builder.push("rope_arrow");
            ROPE_ARROW_ROPE = builder.comment("If you really don't like my ropes you can specify here the block id of"+
                    "a rope from another mod which will get deployed by rope arrows instead of mine")
                    .define("rope_arrow_override","supplementaries:rope");
            builder.pop();
            //flute
            builder.push("flute");
            FLUTE_RADIUS = builder.comment("radius in which an unbound flute will search pets")
                    .defineInRange("unbound_radius",64, 0, 500);
            FLUTE_DISTANCE = builder.comment("max distance at which a bound flute will allow a pet to teleport")
                    .defineInRange("bound_distance",64, 0, 500);

            builder.pop();
            //bomb
            builder.push("bomb");
            BOMB_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .defineInRange("explosion_radius",2, 0.1, 10);
            BOMB_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .define("break_blocks",false);

            builder.pop();


            builder.pop();
        }

    }

    public static class tweaks {
        public static ForgeConfigSpec.BooleanValue DIRECTIONAL_CAKE;
        public static ForgeConfigSpec.BooleanValue DOUBLE_CAKE_PLACEMENT;
        public static ForgeConfigSpec.BooleanValue HANGING_POT_PLACEMENT;
        public static ForgeConfigSpec.BooleanValue WALL_LANTERN_PLACEMENT;
        public static ForgeConfigSpec.BooleanValue THROWABLE_BRICKS_ENABLED;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> WALL_LANTERN_BLACKLIST;
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
        public static ForgeConfigSpec.BooleanValue ZOMBIE_HORSE;
        public static ForgeConfigSpec.IntValue ZOMBIE_HORSE_COST;
        public static ForgeConfigSpec.BooleanValue ENCHANTMENT_BYPASS;

        private static void init(ForgeConfigSpec.Builder builder){
            builder.comment("Vanilla tweaks")
                    .push("tweaks");

            //double cake
            builder.push("cake_tweaks");
            DOUBLE_CAKE_PLACEMENT = builder.comment("allows you to place a cake ontop of another")
                    .define("double_cake",true);
            DIRECTIONAL_CAKE = builder.comment("replaces normal cake placement with a directional one")
                    .define("directional_cake",true);
            builder.pop();

            //hanging pot
            builder.push("hanging_flower_pots");
            HANGING_POT_PLACEMENT = builder.comment("allows you to place hanging flower pots. Works with any modded pot too")
                    .define("enabled",true);
            builder.pop();

            //throwable bricks
            builder.push("throwable_bricks");
            THROWABLE_BRICKS_ENABLED = builder.comment("throw bricks at your foes! Might break glass blocks")
                    .define("enabled",true);
            builder.pop();
            //wall lantern
            builder.push("wall_lantern");
            WALL_LANTERN_PLACEMENT = builder.comment("allow wall lanterns placement")
                    .define("enabled",true);

            List<String> modBlacklist = Arrays.asList("extlights","betterendforge","tconstruct");
            WALL_LANTERN_BLACKLIST = builder.comment("mod ids of mods that have lantern block that extend the base lantern class but don't look like one")
                    .defineList("mod_blacklist", modBlacklist,s -> true);
            builder.pop();
            //bells
            builder.push("bells_tweaks");
            BELL_CHAIN = builder.comment("Ring a bell by clicking on a chain that's connected to it")
                    .define("chain_ringing",true);
            BELL_CHAIN_LENGTH = builder.comment("Max chain length that allows a bell to ring")
                    .defineInRange("chain_length",16,0,256);
            builder.pop();

            builder.push("placeable_sticks");
            PLACEABLE_STICKS = builder.comment("allow placeable sticks")
                    .define("enabled",true);
            PLACEABLE_RODS = builder.comment("allow placeable blaze rods")
                    .define("enabled",true);
            builder.pop();

            builder.push("raked_gravel");
            RAKED_GRAVEL = builder.comment("allow gravel to be raked with a hoe")
                    .define("enabled",true);
            builder.pop();

            builder.push("bottle_xp");
            BOTTLE_XP = builder.comment("Allow bottling up xp by using a bottle on an enchanting table")
                    .define("enabled",true);
            BOTTLING_COST = builder.comment("bottling health cost")
                    .defineInRange("cost",2,0,20);
            builder.pop();

            builder.push("map_tweaks");
            CUSTOM_ADVENTURER_MAPS_TRADES = builder.comment("In this section you can add custom structure maps to cartographers\n"+
                    "The format required is as follows:\n"+
                    "[[<structure>,<level>,<min_price>,<max_price>,<map_name>,<map_color>,<map_marker>],[<structure>,...,<map_marker>],...]\n"+
                    "With the following parameters:\n"+
                    " - <structure> structure id to be located (ie: minecraft:igloo)\n"+
                    " - <level> villager trading level at which the map will be sold. Must be between 1 and 5\n"+
                    " - <min_price> minimum emerald price\n"+
                    " - <max_price> maximum emerald price\n"+
                    " - <map_name> map item name\n"+
                    " - <map_color> hex color of the map item overlay texture\n"+
                    " - <map_marker> id of the map marker to be used (ie: supplementaries:igloo). \n"+
                    "See texture folder for all the names. Leave empty for default ones\n"+
                    "Note that ony the first parameter is required, each of the others others can me removed and will be defaulted to reasonable values\n"+
                    "example: ['minecraft:swamp_hut','2','5','7','witch hut map','0x00ff33']")

                    .defineList("custom_adventurer_maps", Collections.singletonList(Collections.singletonList("")), s->true);
            RANDOM_ADVENTURER_MAPS = builder.comment("Cartographers will sell 'adventurer maps' that will lead to a random vanilla structure (choosen from a thought out preset list).\n"+
                    "Best kept disabled if you are adding custom adventurer maps with its config")
                    .define("random_adventurer_maps",true);
            MAP_MARKERS = builder.comment("enables beacons, lodestones, respawn anchors, beds, conduits, portals to be displayed on maps by clicking one of them with a map")
                    .define("block_map_markers",true);
            builder.pop();

            builder.push("ceiling_banners");
            CEILING_BANNERS = builder.comment("Allow banners to be placed on ceilings")
                    .define("enabled",true);
            builder.pop();

            builder.push("zombie_horse");
            ZOMBIE_HORSE = builder.comment("Feed a stack of rotten flesh to a skeleton horse to buff him up to a zombie horse")
                    .define("zombie_horse_conversion",true);
            ZOMBIE_HORSE_COST = builder.comment("Amount of rotten flesh needed")
                    .defineInRange("rotten_flesh",64,1,1000);
            builder.pop();

            builder.push("enchanting_table");
            ENCHANTMENT_BYPASS = builder.comment("Allows enchanting table to interact with bokshelf even if they have carpets or other tagged items in the way. You can add more blocks by adding them to 'enchantment_bypass' block tag")
                    .define("ignore_carpets",true);
            builder.pop();


            builder.pop();
        }

    }

    public static class block {
        public static ForgeConfigSpec.IntValue GLOBE_TRADES;
        public static ForgeConfigSpec.DoubleValue GLOBE_TREASURE_CHANCHE;

        public static ForgeConfigSpec.IntValue SPEAKER_RANGE;

        public static ForgeConfigSpec.IntValue BELLOWS_PERIOD;
        public static ForgeConfigSpec.IntValue BELLOWS_POWER_SCALING;
        public static ForgeConfigSpec.DoubleValue BELLOWS_MAX_VEL;
        public static ForgeConfigSpec.DoubleValue BELLOWS_BASE_VEL_SCALING;
        public static ForgeConfigSpec.BooleanValue BELLOWS_FLAG;
        public static ForgeConfigSpec.IntValue BELLOWS_RANGE;

        public static ForgeConfigSpec.DoubleValue LAUNCHER_VEL;
        public static ForgeConfigSpec.IntValue LAUNCHER_HEIGHT;

        public static ForgeConfigSpec.IntValue TURN_TABLE_PERIOD;
        public static ForgeConfigSpec.BooleanValue TURN_TABLE_ROTATE_ENTITIES;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> TURN_TABLE_BLACKLIST;

        public static ForgeConfigSpec.IntValue JAR_CAPACITY;
        public static ForgeConfigSpec.BooleanValue JAR_EAT;

        public static ForgeConfigSpec.BooleanValue CAGE_ALL_MOBS;
        public static ForgeConfigSpec.BooleanValue CAGE_ALL_BABIES;

        public static ForgeConfigSpec.BooleanValue NOTICE_BOARDS_UNRESTRICTED;

        public static ForgeConfigSpec.BooleanValue SACK_PENALTY;
        public static ForgeConfigSpec.IntValue SACK_INCREMENT;
        public static ForgeConfigSpec.IntValue SACK_SLOTS;

        public static ForgeConfigSpec.BooleanValue SAFE_UNBREAKABLE;
        public static ForgeConfigSpec.BooleanValue SAFE_SIMPLE;

        public static ForgeConfigSpec.BooleanValue BLACKBOARD_COLOR;

        public static ForgeConfigSpec.IntValue CANDLE_HOLDER_LIGHT;

        public static ForgeConfigSpec.BooleanValue REPLACE_DAUB;

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

        public static ForgeConfigSpec.BooleanValue STICK_POLE;
        public static ForgeConfigSpec.IntValue STICK_POLE_LENGTH;

        private static void init(ForgeConfigSpec.Builder builder){

            builder.comment("Server side blocks configs")
                    .push("blocks");

            //globe
            builder.push("globe");
            GLOBE_TRADES = builder.comment("how many globe trades to give to the wandering trader. This will effectively increase the chance of him having a globe trader. Increase this if you have other mods that add stuff to that trader")
                    .defineInRange("chance",2,0,50);
            GLOBE_TREASURE_CHANCHE = builder.comment("chanche of finding a globe in a shipwreck treasure chest.")
                    .defineInRange("shipwreck_treasure_chance",0.25,0,1);
            builder.pop();

            //speaker
            builder.push("speaker_block");
            SPEAKER_RANGE = builder.comment("Maximum block range")
                    .defineInRange("range", 64, 0, 100000000);
            builder.pop();
            //bellows
            builder.push("bellows");
            BELLOWS_PERIOD = builder.comment("bellows pushes air following this equation: \n"+
                    "air=(sin(2PI*ticks/period)<0), with period = base_period-(redstone_power-1)*power_scaling \n"+
                    "represents base period at 1 power")
                    .defineInRange("base_period", 78, 1, 512);
            BELLOWS_POWER_SCALING = builder.comment("how much the period changes in relation to the block redstone power")
                    .defineInRange("power_scaling", 3, 0, 128);
            BELLOWS_BASE_VEL_SCALING = builder.comment("velocity increase uses this equation: \n"+
                    "vel = base_vel*((range-entity_distance)/range) with base_vel = base_velocity_scaling/period \n"+
                    "note that the block will push further the faster it's pulsing")
                    .defineInRange("base_velocity_scaling", 5.0, 0.0, 64);
            BELLOWS_MAX_VEL = builder.comment("entities with velocity greated than this won't be pushed")
                    .defineInRange("power_scaling", 2.0, 0.0, 16);
            BELLOWS_FLAG = builder.comment("sets velocity changed flag when pushing entities +\n"+
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
                    .defineInRange("fall_height_required",5,0, 512);
            builder.pop();
            //turn table
            builder.push("turn_table");
            //TURN_TABLE_PERIOD = builder.comment("how many ticks it takes to rotate a block/entity")
            //        .defineInRange("period", 20, 1, 256);
            TURN_TABLE_ROTATE_ENTITIES = builder.comment("can rotate entities standing on it?")
                    .define("rotate_entities", true);
            List<String> turnTableBlacklist = Arrays.asList("minecraft:end_portal_frame");
            TURN_TABLE_BLACKLIST = builder.comment("blocks that can't be rotated. Some special ones like chests, beds and pistons are already hardcoded")
                    .defineList("blacklist", turnTableBlacklist,s -> true);
            builder.pop();
            //jar
            builder.push("jar");
            JAR_CAPACITY = builder.comment("jar liquid capacity: leave at 12 for pixel accuracy")
                    .defineInRange("capacity",12,0,1024);
            JAR_EAT = builder.comment("allow right click to instantly eat or drink food or potions inside a jar.\n" +
                    "Disable if you think this ability is op. Cookies are excluded")
                    .define("drink_from_jar",true);

            builder.pop();

            //cage
            builder.push("cage");
            CAGE_ALL_MOBS = builder.comment("allow all entities to be captured by cages and jars. Not meant for survival")
                    .define("cage_allow_all_mobs", false);
            CAGE_ALL_BABIES = builder.comment("allow all baby mobs to be captured by cages")
                    .define("cage_allow_all_babies", false);
            builder.pop();

            //notice boards
            builder.push("notice_board");
            NOTICE_BOARDS_UNRESTRICTED = builder.comment("allow notice boards to accept and display any item, not just maps and books")
                    .define("allow_any_item", false);
            builder.pop();

            builder.push("sack");
            SACK_PENALTY = builder.comment("Penalize the player with slowness effect when carrying too many sacks")
                    .define("sack_penality", true);
            SACK_INCREMENT = builder.comment("maximum number of sacks after which the slowness effect will be applied. each multiple of this number will further slow the player down")
                    .defineInRange("sack_increment",2,0,50);
            SACK_SLOTS = builder.comment("How many slots should a sack have")
                    .defineInRange("slots",9,1,27);
            builder.pop();

            builder.push("safe");
            SAFE_UNBREAKABLE = builder.comment("Makes safes only breakable by their owner or by a player in creative")
                    .define("prevent_breaking",false);
            SAFE_SIMPLE = builder.comment("Make safes simpler so they do not require keys:\n" +
                    "they will be bound to the first person that opens one and only that person will be able to interact with them")
                    .define("simple_safes",false);
            builder.pop();

            builder.push("blackboard");
            BLACKBOARD_COLOR = builder.comment("Enable to draw directly on a blackboard using any dye. Gui still only works in black and white")
                    .define("colored_blackboard",false);
            builder.pop();

            builder.push("candle_holder");
            CANDLE_HOLDER_LIGHT = builder.comment("Candle holder light level")
                    .defineInRange("light_level",12,1,15);


            builder.pop();

            builder.push("timber_frame");
            REPLACE_DAUB = builder.comment("Replace a timber frame with wattle and daub block when daub is placed in it")
                    .define("replace_daub",true);


            builder.pop();

            builder.push("hourglass");
            HOURGLASS_SUGAR = builder.comment("Time in ticks for sugar")
                    .defineInRange("sugar_time",40,0,10000);
            HOURGLASS_SAND = builder.comment("Time in ticks for sand blocks")
                    .defineInRange("sand_time",70,0,10000);
            HOURGLASS_CONCRETE = builder.comment("Time in ticks for concrete blocks")
                    .defineInRange("concrete_time",105,0,10000);
            HOURGLASS_DUST = builder.comment("Time in ticks for generic dust")
                    .defineInRange("dust_time",150,0,10000);
            HOURGLASS_GLOWSTONE = builder.comment("Time in ticks for glowstone dust")
                    .defineInRange("glowstone_time",190,0,10000);
            HOURGLASS_BLAZE_POWDER = builder.comment("Time in ticks for blaze powder")
                    .defineInRange("blaze_powder_time",277,0,10000);
            HOURGLASS_REDSTONE = builder.comment("Time in ticks for redstone dust")
                    .defineInRange("redstone_time",400,0,10000);
            HOURGLASS_SLIME = builder.comment("Time in ticks for slime balls")
                    .defineInRange("slime_time",1750,0,10000);
            HOURGLASS_HONEY = builder.comment("Time in ticks for honey")
                    .defineInRange("honey_time",2000,0,10000);
            builder.pop();

            builder.push("item_shelf");
            ITEM_SHELF_LADDER = builder.comment("Makes item shelves climbable")
                    .define("climbable_shelves",false);
            builder.pop();

            builder.push("iron_gate");
            DOUBLE_IRON_GATE = builder.comment("Allows two iron gates to be opened simultaneously when on top of the other")
                    .define("double_opening", true);
            builder.pop();

            builder.push("flag");
            STICK_POLE = builder.comment("Allows right/left clicking on a stick to lower/raise a flag attached to it")
                    .define("stick_pole", true);
            STICK_POLE_LENGTH = builder.comment("Maximum allowed pole length")
                    .defineInRange("pole_length",16,0,256);
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


        public static ForgeConfigSpec.ConfigValue<List<? extends String>> SIGNS_VILLAGES;
        public static ForgeConfigSpec.BooleanValue DISTANCE_TEXT;
        public static ForgeConfigSpec.IntValue ROAD_SIGN_DISTANCE_MIN;
        public static ForgeConfigSpec.IntValue ROAD_SIGN_DISTANCE_AVR;

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Configure spawning conditions")
                    .push("spawns");
            builder.push("firefly");
            List<String> defaultBiomes = Arrays.asList("minecraft:swamp","minecraft:swamp_hills","minecraft:plains",
                    "minecraft:sunflower_plains","minecraft:dark_forest","minecraft:dark_forest_hills", "byg:bayou",
                    "byg:cypress_swamplands", "byg:glowshroom_bayou", "byg:mangrove_marshes", "byg:vibrant_swamplands",
                    "byg:fresh_water_lake", "byg:grassland_plateau", "byg:wooded_grassland_plateau", "byg:flowering_grove",
                    "byg:guiana_shield", "byg:guiana_clearing", "byg:meadow", "byg:orchard", "byg:seasonal_birch_forest",
                    "byg:seasonal_deciduous_forest", "byg:seasonal_forest", "biomesoplenty:flower_meadow", "biomesoplenty:fir_clearing",
                    "biomesoplenty:grove_lakes", "biomesoplenty:grove", "biomesoplenty:highland_moor", "biomesoplenty:wetland_marsh",
                    "biomesoplenty:deep_bayou","biomesoplenty:wetland");
            List<String> fireflyModWhitelist = Arrays.asList();
            //TODO add validation for biomes
            FIREFLY_BIOMES = builder.comment("Spawnable biomes")
                    .defineList("biomes", defaultBiomes, s -> true);
            FIREFLY_MOD_WHITELIST = builder.comment("Whitelisted mods. All biomes from said mods will be able to spawn fireflies. Use the one above for more control")
                    .defineList("mod_whitelist", fireflyModWhitelist, s -> true);
            FIREFLY_WEIGHT = builder.comment("Spawn weight \n"+
                    "Set to 0 to disable spawning entirely")
                    .defineInRange("weight", 3, 0, 100);
            FIREFLY_MIN = builder.comment("Minimum group size")
                    .defineInRange("min", 5, 0, 64);
            FIREFLY_MAX = builder.comment("Maximum group size")
                    .defineInRange("max", 9, 0, 64);

            builder.pop();

            builder.push("structures");
            builder.push("way_sign");
            ROAD_SIGN_DISTANCE_AVR = builder.comment("Average distance apart in chunks between spawn attempts")
                    .defineInRange("average_distance",19,0,1001);
            ROAD_SIGN_DISTANCE_MIN = builder.comment("Minimum distance apart in chunks between spawn attempts. 1001 to disable them entirely")
                    .defineInRange("minimum_distance",10,0,1001);


            DISTANCE_TEXT = builder.comment("With this option road signs will display the distance to the structure that they are pointing to")
                    .define("show_distance_text",true);


            List<String> villages = Arrays.asList("minecraft:village","repurposed_structures:village_badlands","repurposed_structures:village_dark_oak","repurposed_structures:village_birch",
                    "repurposed_structures:village_giant_taiga","repurposed_structures:village_jungle","repurposed_structures:village_mountains","repurposed_structures:village_oak",
                    "repurposed_structures:village_swamp","pokecube:village","pokecube_legends:village","pokecube_legends:village/ocean",
                    "valhelsia_structures:castle","valhelsia_structures:castle_ruin","valhelsia_structures:small_castle","valhelsia_structures:tower_ruin",
                    "stoneholm:underground_village");

            SIGNS_VILLAGES = builder.comment("list of structure that a sign can point to. Note that they will only spawn in dimensions where vanilla villages can")
                    .defineList("villages", villages, s->true);

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
            FIREFLY_PERIOD = builder.comment("firefly animation period\n"+
                    "note that actual period will be this + a random number between 0 and 10\n"+
                    "this needs to be here to allow correct despawning of the entity when it's not glowing\n"+
                    "check client configs come more animation settings")
                    .defineInRange("period", 65, 1, 200);
            FIREFLY_SPEED = builder.comment("firefly flying speed")
                    .defineInRange("speed", 0.3, 0, 10);
            FIREFLY_DESPAWN = builder.comment("despawn during the day")
                    .define("despawn",true);
            builder.pop();

            builder.pop();
        }
    }



    //maybe not need but hey
    public static class cached{
        //items
        public static String ROPE_ARROW_ROPE;
        public static Block ROPE_ARROW_BLOCK;
        public static int FLUTE_RADIUS;
        public static int FLUTE_DISTANCE;
        public static float BOMB_RADIUS;
        public static boolean BOMB_BREAKS;
        //tweaks
        public static int ZOMBIE_HORSE_COST;
        public static boolean ZOMBIE_HORSE;
        public static boolean DIRECTIONAL_CAKE;
        public static boolean DOUBLE_CAKE_PLACEMENT;
        public static boolean HANGING_POT_PLACEMENT;
        public static boolean THROWABLE_BRICKS_ENABLED;
        public static boolean WALL_LANTERN_PLACEMENT;
        public static List<? extends String> WALL_LANTERN_BLACKLIST;
        public static boolean BELL_CHAIN;
        public static int BELL_CHAIN_LENGTH;
        public static boolean PLACEABLE_STICKS;
        public static boolean PLACEABLE_RODS;
        public static boolean RAKED_GRAVEL;
        public static boolean BOTTLE_XP;
        public static int BOTTLING_COST;
        public static boolean MAP_MARKERS;
        public static boolean CEILING_BANNERS;
        public static boolean ENCHANTMENT_BYPASS;
        //spawns
        public static int FIREFLY_MIN;
        public static int FIREFLY_MAX;
        public static int FIREFLY_WEIGHT;
        public static List<? extends String> FIREFLY_BIOMES;
        public static List<? extends String> FIREFLY_MOD_WHITELIST;
        public static boolean FIREFLY_DESPAWN;
        public static boolean DISTANCE_TEXT;
        //blocks
        public static int SPEAKER_RANGE;
        public static int BELLOWS_PERIOD;
        public static int BELLOWS_POWER_SCALING;
        public static double BELLOWS_MAX_VEL;
        public static double BELLOWS_BASE_VEL_SCALING;
        public static boolean BELLOWS_FLAG;
        public static int BELLOWS_RANGE;
        public static double LAUNCHER_VEL;
        public static int LAUNCHER_HEIGHT;
        public static int TURN_TABLE_PERIOD;
        public static boolean TURN_TABLE_ROTATE_ENTITIES;
        public static List<? extends String> TURN_TABLE_BLACKLIST;
        public static int JAR_CAPACITY;
        public static boolean JAR_EAT;
        public static boolean NOTICE_BOARDS_UNRESTRICTED;
        public static boolean CAGE_ALL_MOBS;
        public static boolean CAGE_ALL_BABIES;
        public static int SACK_INCREMENT;
        public static boolean SACK_PENALTY;
        public static int SACK_SLOTS;
        public static boolean SAFE_UNBREAKABLE;
        public static boolean SAFE_SIMPLE;
        public static int GLOBE_TRADES;
        public static double GLOBE_TREASURE_CHANCE;
        public static boolean BLACKBOARD_COLOR;
        public static int CANDLE_HOLDER_LIGHT;
        public static boolean REPLACE_DAUB;
        public static boolean ITEM_SHELF_LADDER;
        public static boolean DOUBLE_IRON_GATE;
        public static boolean STICK_POLE;
        public static int STICK_POLE_LENGTH;

        //entity
        public static int FIREFLY_PERIOD;
        public static double FIREFLY_SPEED;

        public static void refresh(){
            ZOMBIE_HORSE_COST = tweaks.ZOMBIE_HORSE_COST.get();
            ZOMBIE_HORSE = tweaks.ZOMBIE_HORSE.get();
            BOTTLING_COST = tweaks.BOTTLING_COST.get();
            BOTTLE_XP = tweaks.BOTTLE_XP.get();
            RAKED_GRAVEL = tweaks.RAKED_GRAVEL.get() && RegistryConfigs.reg.RAKED_GRAVEL_ENABLED.get();
            PLACEABLE_RODS = tweaks.PLACEABLE_RODS.get() && RegistryConfigs.reg.ROD_ENABLED.get();
            PLACEABLE_STICKS = tweaks.PLACEABLE_STICKS.get() && RegistryConfigs.reg.STICK_ENABLED.get();
            DIRECTIONAL_CAKE = tweaks.DIRECTIONAL_CAKE.get();
            DOUBLE_CAKE_PLACEMENT = tweaks.DOUBLE_CAKE_PLACEMENT.get();
            HANGING_POT_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            WALL_LANTERN_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            THROWABLE_BRICKS_ENABLED = tweaks.THROWABLE_BRICKS_ENABLED.get();
            WALL_LANTERN_BLACKLIST = tweaks.WALL_LANTERN_BLACKLIST.get();
            BELL_CHAIN = tweaks.BELL_CHAIN.get();
            BELL_CHAIN_LENGTH = tweaks.BELL_CHAIN_LENGTH.get();
            MAP_MARKERS = tweaks.MAP_MARKERS.get();
            CEILING_BANNERS = tweaks.CEILING_BANNERS.get();
            ENCHANTMENT_BYPASS = tweaks.ENCHANTMENT_BYPASS.get();

            ROPE_ARROW_ROPE = item.ROPE_ARROW_ROPE.get();
            ROPE_ARROW_BLOCK = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ROPE_ARROW_ROPE));
            if(ROPE_ARROW_BLOCK == Blocks.AIR)ROPE_ARROW_BLOCK = Registry.ROPE.get();
            FLUTE_DISTANCE = item.FLUTE_DISTANCE.get();
            FLUTE_RADIUS = item.FLUTE_RADIUS.get();
            BOMB_BREAKS = item.BOMB_BREAKS.get();
            BOMB_RADIUS = (float)(item.BOMB_RADIUS.get()+0f);

            FIREFLY_MIN = spawn.FIREFLY_MIN.get();
            FIREFLY_MAX = spawn.FIREFLY_MAX.get();
            FIREFLY_WEIGHT = spawn.FIREFLY_WEIGHT.get();
            FIREFLY_BIOMES = spawn.FIREFLY_BIOMES.get();
            FIREFLY_MOD_WHITELIST = spawn.FIREFLY_MOD_WHITELIST.get();

            DISTANCE_TEXT = spawn.DISTANCE_TEXT.get();

            GLOBE_TRADES = block.GLOBE_TRADES.get();
            GLOBE_TREASURE_CHANCE = block.GLOBE_TREASURE_CHANCHE.get();

            SPEAKER_RANGE = block.SPEAKER_RANGE.get();

            BELLOWS_PERIOD = block.BELLOWS_PERIOD.get();
            BELLOWS_POWER_SCALING = block.BELLOWS_POWER_SCALING.get();
            BELLOWS_MAX_VEL = block.BELLOWS_MAX_VEL.get();
            BELLOWS_BASE_VEL_SCALING = block.BELLOWS_BASE_VEL_SCALING.get();
            BELLOWS_FLAG = block.BELLOWS_FLAG.get();
            BELLOWS_RANGE = block.BELLOWS_RANGE.get();

            LAUNCHER_VEL = block.LAUNCHER_VEL.get();
            LAUNCHER_HEIGHT = block.LAUNCHER_HEIGHT.get();

            //TURN_TABLE_PERIOD = block.TURN_TABLE_PERIOD.get();
            TURN_TABLE_ROTATE_ENTITIES = block.TURN_TABLE_ROTATE_ENTITIES.get();
            TURN_TABLE_BLACKLIST = block.TURN_TABLE_BLACKLIST.get();

            JAR_CAPACITY = block.JAR_CAPACITY.get();
            JAR_EAT = block.JAR_EAT.get();

            NOTICE_BOARDS_UNRESTRICTED = block.NOTICE_BOARDS_UNRESTRICTED.get();

            CAGE_ALL_MOBS = block.CAGE_ALL_MOBS.get();
            CAGE_ALL_BABIES = block.CAGE_ALL_BABIES.get();

            SACK_INCREMENT = block.SACK_INCREMENT.get();
            SACK_PENALTY = block.SACK_PENALTY.get();
            SACK_SLOTS = block.SACK_SLOTS.get();

            SAFE_UNBREAKABLE= block.SAFE_UNBREAKABLE.get();
            SAFE_SIMPLE = block.SAFE_SIMPLE.get();

            BLACKBOARD_COLOR = block.BLACKBOARD_COLOR.get();

            CANDLE_HOLDER_LIGHT = block.CANDLE_HOLDER_LIGHT.get();

            REPLACE_DAUB = block.REPLACE_DAUB.get();

            ITEM_SHELF_LADDER = block.ITEM_SHELF_LADDER.get();

            DOUBLE_IRON_GATE = block.DOUBLE_IRON_GATE.get();

            STICK_POLE = block.STICK_POLE.get();
            STICK_POLE_LENGTH = block.STICK_POLE_LENGTH.get();

            FIREFLY_PERIOD = entity.FIREFLY_PERIOD.get();
            FIREFLY_SPEED = entity.FIREFLY_SPEED.get();
            FIREFLY_DESPAWN = entity.FIREFLY_DESPAWN.get();

        }
    }
}