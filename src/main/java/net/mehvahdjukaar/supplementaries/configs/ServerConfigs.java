package net.mehvahdjukaar.supplementaries.configs;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServerConfigs {
    public static ForgeConfigSpec SERVER_CONFIG;

    public static ForgeConfigSpec.BooleanValue comment;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("comment");
        builder.comment("This config is synced server side. If you are on a server you'll get its own version");
        comment = builder.define("comment",true);
        builder.pop();

        //reg.init(SERVER_BUILDER);

        block.init(builder);
        spawn.init(builder);
        entity.init(builder);
        tweaks.init(builder);
        item.init(builder);

        SERVER_CONFIG = builder.build();
    }


    public static class item {
        public static ForgeConfigSpec.IntValue FLUTE_RADIUS;
        public static ForgeConfigSpec.IntValue FLUTE_DISTANCE;


        private static void init(ForgeConfigSpec.Builder builder){
            builder.push("items");

            //flute
            builder.push("flute");
            FLUTE_RADIUS = builder.comment("radius in which an unbound flute will search pets")
                    .defineInRange("unbound_radius",64, 0, 500);
            FLUTE_DISTANCE = builder.comment("max distance at which a bound flute will allow a pet to teleport")
                    .defineInRange("bound_distance",64, 0, 500);
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
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> BRICKS_LIST;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> WALL_LANTERN_BLACKLIST;
        public static ForgeConfigSpec.BooleanValue BELL_CHAIN;
        public static ForgeConfigSpec.IntValue BELL_CHAIN_LENGTH;

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
            THROWABLE_BRICKS_ENABLED = builder.comment("throw bricks at your foes! Might glass blocks")
                    .define("enabled",true);
            List<String> bricksList = Arrays.asList("architects_palette:algal_brick", "architects_palette:sunmetal_brick", "ars_nouveau:arcane_brick", "biomesoplenty:mud_brick", "byg:yellow_nether_brick",
                    "byg:blue_nether_brick", "endergetic:eumus_brick", "extcaves:half_brick");
            BRICKS_LIST = builder.comment("additional items that will be able to be thrown (will work with any item). Items tagges as forge/ingots/bricks or nether bricks will be automatically added")
                    .defineList("whitelist",bricksList,s -> true);
            builder.pop();
            //wall lantern
            builder.push("wall_lantern");
            WALL_LANTERN_PLACEMENT = builder.comment("allow wall lanterns placement")
                    .define("enabled",true);

            List<String> modBlacklist = Arrays.asList("extlights");
            WALL_LANTERN_BLACKLIST = builder.comment("mod ids of mods that have lantern block that extend the base lantern class but don't look like one")
                    .defineList("mod_blacklist", modBlacklist,s -> true);
            builder.pop();
            //bells
            builder.push("bells_tweaks");
            BELL_CHAIN = builder.comment("ring a bell by clicking on a chain that's connected to it")
                    .define("chain_ringing",true);
            BELL_CHAIN_LENGTH = builder.comment("max chain length that allows a bell to ring")
                    .defineInRange("chain_length",16,0,1024);
            builder.pop();


            builder.pop();
        }

    }

    public static class block {
        public static ForgeConfigSpec.IntValue GLOBE_TRADES;
        public static ForgeConfigSpec.DoubleValue GLOBE_TREASURE_CHANCHE;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> SIGN_POST_ADDITIONAL;

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
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> JAR_COOKIES;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_ALLOWED_MOBS;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_TINTED_ALLOWED_MOBS;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> CAGE_ALLOWED_MOBS;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> CAGE_ALLOWED_BABY_MOBS;
        public static ForgeConfigSpec.BooleanValue CAGE_ALL_MOBS;

        public static ForgeConfigSpec.BooleanValue NOTICE_BOARDS_UNRESTRICTED;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> SACK_WHITELIST;
        public static ForgeConfigSpec.BooleanValue SACK_PENALTY;
        public static ForgeConfigSpec.IntValue SACK_INCREMENT;
        public static ForgeConfigSpec.IntValue SACK_SLOTS;

        public static ForgeConfigSpec.BooleanValue SAFE_UNBREAKABLE;

        private static void  init(ForgeConfigSpec.Builder builder){

            builder.comment("Server side blocks configs")
                    .push("blocks");

            //globe
            builder.push("globe");
            GLOBE_TRADES = builder.comment("how many globe trades to give to the wandering trader. If you have mods that add more trades to him you might want to increase this so it's not as rare")
                    .defineInRange("trades",2,0,50);
            GLOBE_TREASURE_CHANCHE = builder.comment("chanche of finding a globe in a shipwreck treasure chest.")
                    .defineInRange("shipwreck_treasure_chance",0.25,0,1);
            builder.pop();

            //sign post
            builder.push("sign_post");
            List<String> signPostWhitelist = Arrays.asList("adorn:oak_post", "adorn:birch_post", "adorn:spruce_post", "adorn:acacia_post",
                    "adorn:dark_oak_post", "adorn:jungle_post", "adorn:warped_post", "adorn:crimson_post", "adorn:stone_post",
                    "adorn:cobblestone_post", "adorn:sandstone_post", "adorn:diorite_post", "adorn:andesite_post", "adorn:granite_post",
                    "adorn:brick_post", "adorn:stone_brick_post", "adorn:red_sandstone_post", "adorn:nether_brick_post", "adorn:basalt_post",
                    "adorn:blackstone_post", "adorn:red_nether_brick_post", "adorn:prismarine_post", "adorn:quartz_post",
                    "adorn:end_stone_brick_post", "adorn:purpur_post", "adorn:polished_blackstone_post", "adorn:polished_blackstone_brick_post",
                    "adorn:polished_diorite_post", "adorn:polished_andesite_post", "adorn:polished_granite_post", "adorn:prismarine_brick_post",
                    "adorn:dark_prismarine_post", "adorn:cut_snadstone_post", "adorn:smooth_sandstone_post", "adorn:cut_red_sandstone_post",
                    "adorn:smooth_red_sandstone_post", "adorn:smooth_stone_post", "adorn:mossy_cobblestone_post", "adorn:mossy_stone_brick_post",
                    "car:sign_post", "mysticalworld:thatch_small_post", "mysticalworld:red_mushroom_small_post", "mysticalworld:brown_mushroom_small_post",
                    "mysticalworld:mushroom_stem_small_post", "mysticalworld:mushroom_inside_small_post", "mysticalworld:mud_block_small_post",
                    "mysticalworld:mud_brick_small_post", "mysticalworld:charred_small_post", "mysticalworld:terracotta_brick_small_post",
                    "mysticalworld:iron_brick_small_post", "mysticalworld:soft_stone_small_post", "mysticalworld:cracked_stone_small_post",
                    "mysticalworld:blackened_stone_small_post", "mysticalworld:soft_obsidian_small_post", "mysticalworld:amethyst_small_post",
                    "mysticalworld:copper_small_post", "mysticalworld:lead_small_post", "mysticalworld:quicksilver_small_post", "mysticalworld:silver_small_post",
                    "mysticalworld:tin_small_post", "quark:oak_post", "quark:birch_post", "quark:spruce_post", "quark:acacia_post", "quark:dark_oak_post",
                    "quark:jungle_post", "quark:warped_post", "quark:crimson_post", "quark:stripped_oak_post", "quark:stripped_birch_post",
                    "quark:stripped_spruce_post", "quark:stripped_acacia_post", "quark:stripped_dark_oak_post", "quark:stripped_jungle_post",
                    "quark:stripped_warped_post", "quark:stripped_crimson_post");
            SIGN_POST_ADDITIONAL = builder.comment("additional blocks besides fences that can accept a sign post")
                    .defineList("whitelist", signPostWhitelist,s -> true);
            builder.pop();

            //speaker
            builder.push("speaker_block");
            SPEAKER_RANGE = builder.comment("maximum range")
                    .defineInRange("range", 64, 0, 256);
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

            List<String> cookies = Arrays.asList("minecraft:cookie","farmersdelight:honey_cookie","farmersdelight:sweet_berry_cookie",
                    "farmersdelight:peanut_butter_cookie","pamhc2crops:beanitem", "pamhc2crops:chickpeaitem",
                    "cookielicious:strawberry_cookie", "cookielicious:vanilla_cookie", "cookielicious:sandwich_cookie",
                    "pamhc2crops:garlicitem","pamhc2crops:jicamaitem","pamhc2crops:roastedmushroomitem",
                    "pamhc2crops:bakedwaterchestnutitem","pamhc2crops:waterchestnutitem","pamhc2crops:chocolatemuffinitem",
                    "pamhc2crops:donutitem","pamhc2crops:chocolatedonutitem","pamhc2crops:jellydonutitem","pamhc2crops:crackeritem",
                    "pamhc2crops:pretzelitem","pamhc2crops:chocolatebaritem","pamhc2crops:chocolaterollitem","pamhc2crops:chocolatecaramelfudgeitem",
                    "pamhc2crops:smoresitem","pamhc2crops:trailmixitem","pamhc2crops:candiedpecansitem",
                    "pamhc2crops:candiedsweetpotatoesitem","pamhc2crops:candiedwalnutsitem","pamhc2crops:chocolateorangeitem",
                    "pamhc2crops:chocolatepeanutbaritem","pamhc2crops:chocolatestrawberryitem","pamhc2crops:peanutbuttercupitem",
                    "pamhc2crops:pralinesitem","pamhc2crops:pinenutitem","pamhc2crops:roastedalmonditem","pamhc2crops:roastedpinenutitem",
                    "cookielicious:strawberry_cookie", "cookielicious:vanilla_cookie", "croptopia:raisin_oatmeal_cookie",
                    "croptopia:nutty_cookie", "cspirit:sugar_cookie_santa", "cspirit:sugar_cookie_circle", "cspirit:sugar_cookie_ornament",
                    "cspirit:sugar_cookie_star", "cspirit:sugar_cookie_man", "cspirit:sugar_cookie_snowman", "cspirit:gingerbread_cookie_circle",
                    "inventorypets:holiday_cookie", "simplefarming:peanut_butter_cookie", "teletubbies:toast", "tofucraft:tofucookie");
            JAR_COOKIES = builder.comment("any item can work here, ideally you should only put cookies and alike")
                    .defineList("cookies", cookies,s -> true);


            List<String> jarMobs = Arrays.asList("minecraft:slime",
                    "minecraft:bee","minecraft:magma_cube","iceandfire:pixie","alexsmobs:fly", "alexsmobs:hummingbird","alexsmobs:cockroach",
                    "buzzierbees:honey_slime", "mysticalworld:frog","mysticalworld:beetle","mysticalworld:silkworm",
                    "druidcraft:lunar_moth", "druidcraft:dreadfish","swampexpansion:slabfish",
                    "savageandravage:creepie","betteranimalsplus:butterfly","whisperwoods:moth");
            MOB_JAR_ALLOWED_MOBS = builder.comment("catchable mobs \n"+
                    "due to a vanilla bug some mobs might not render correctly or at all")
                    .defineList("mobs", jarMobs,s -> true);
            List<String> tintedMobs = new ArrayList<>(jarMobs);
            List<String> additionalTinted = Arrays.asList("minecraft:endermite","minecraft:vex","alexsmobs:mimicube");
            tintedMobs.addAll(additionalTinted);
            MOB_JAR_TINTED_ALLOWED_MOBS = builder.comment("tinted jar catchable mobs")
                    .defineList("tinted_jar_mobs", tintedMobs,s -> true);
            builder.pop();

            //cage
            builder.comment("I haven't tested most of the mods included here. let me know if they work")
                    .push("cage");
            List<String> cageMobs = new ArrayList<>(tintedMobs);
            List<String> additionalCageMobs = Arrays.asList("minecraft:parrot","minecraft:rabbit", "minecraft:cat", "minecraft:chicken",
                    "minecraft:bat","minecraft:fox","minecraft:ocelot",
                    "alexsmobs:roadrunner", "alexsmobs:rattlesnake", "alexsmobs:lobster", "alexsmobs:capuchin_monkey",
                    "mysticalworld:silver_fox", "mysticalworld:sprout", "mysticalworld:endermini", "mysticalworld:lava_cat",
                    "mysticalworld:owl","mysticalworld:hell_sprout",
                    "quark:toretoise", "quark:crab", "quark:foxhound", "quark:stoneling", "quark:frog","rats:rat",
                    "rats:piper", "rats:plague_doctor", "rats:black_death", "rats:plague_cloud", "rats:plague_beast", "rats:rat_king",
                    "rats:demon_rat", "rats:ratlantean_spirit", "rats:ratlantean_automation", "rats:feral_ratlantean", "rats:neo_ratlantean",
                    "rats:pirat", "rats:ghost_pirat", "rats:dutchrat", "rats:ratfish", "rats:ratlantean_ratbot", "rats:rat_baron",
                    "goblintraders:goblin_trader", "goblintraders:vein_goblin_trader",
                    "autumnity:snail","betteranimalsplus:lammergeier","betteranimalsplus:songbird",
                    "betteranimalsplus:pheasant", "betteranimalsplus:squirrel", "betteranimalsplus:badger", "betteranimalsplus:turkey",
                    "exoticbirds:roadrunner","exoticbirds:hummingbird","exoticbirds:woodpecker","exoticbirds:kingfisher",
                    "exoticbirds:toucan","exoticbirds:macaw","exoticbirds:magpie", "exoticbirds:kiwi", "exoticbirds:owl",
                    "exoticbirds:gouldianfinch", "exoticbirds:gull", "exoticbirds:pigeon", "exoticbirds:penguin", "exoticbirds:duck",
                    "exoticbirds:booby", "exoticbirds:cardinal", "exoticbirds:bluejay", "exoticbirds:robin", "exoticbirds:kookaburra",
                    "exoticbirds:budgerigar", "exoticbirds:cockatoo","swampexpansion:slabfish");
            cageMobs.addAll(additionalCageMobs);
            CAGE_ALLOWED_MOBS = builder.comment("catchable mobs")
                    .defineList("cage_mobs", cageMobs,s -> true);
            List<String> cageBabyMobs = Arrays.asList("minecraft:cow","minecraft:sheep","minecraft:pig","alexsmobs:crocodile", "alexsmobs:endergrade", "alexsmobs:gazelle", "alexsmobs:gorilla", "alexsmobs:komodo_dragon", "alexsmobs:raccoon", "alexsmobs:seal", "alexsmobs:warped_toad");
            CAGE_ALLOWED_BABY_MOBS = builder.comment("additional mobs that you'll be able to catch with the added condition that it has to be a baby variant. No need to include the ones already in cage_mobs")
                    .defineList("cage_baby_mobs", cageBabyMobs,s -> true);
            CAGE_ALL_MOBS = builder.comment("allow all mobs to be captured by cages")
                    .define("cage_allow_all_mobs", false);
            builder.pop();

            //notice boards
            builder.push("notice_board");
            NOTICE_BOARDS_UNRESTRICTED = builder.comment("allow notice boards to accept and display any item, not just maps and books")
                    .define("allow_any_item", false);
            builder.pop();

            builder.push("sack");
            List<String> sackSupport = Arrays.asList("farmersdelight:rope");
            SACK_WHITELIST = builder.comment("additional blocks that can support a sack")
                    .defineList("whitelist", sackSupport,s -> true);
            SACK_PENALTY = builder.comment("penalize the player with slowness effecn when carring too many sacks")
                    .define("sack_penality", true);
            SACK_INCREMENT = builder.comment("maximum number of sacks after which the slowness effect will be applied. each multiple of this number will further slow the player down")
                    .defineInRange("sack_increment",2,0,50);
            SACK_SLOTS = builder.comment("additional sack slots divided by 2. Number of slots will be 5 + additional_slots*2")
                    .defineInRange("additional_slots",0,0,2);
            builder.pop();

            builder.push("safe");
            SAFE_UNBREAKABLE = builder.comment("makes safes only breakable by their owner or by a player in creative")
                    .define("prevent_breaking",false);
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
                    "biomesoplenty:deep_bayou");
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
        public static int FLUTE_RADIUS;
        public static int FLUTE_DISTANCE;
        //tweaks
        public static boolean DIRECTIONAL_CAKE;
        public static boolean DOUBLE_CAKE_PLACEMENT;
        public static boolean HANGING_POT_PLACEMENT;
        public static boolean THROWABLE_BRICKS_ENABLED;
        public static List<? extends String> BRICKS_LIST;
        public static boolean WALL_LANTERN_PLACEMENT;
        public static List<? extends String> WALL_LANTERN_BLACKLIST;
        public static boolean BELL_CHAIN;
        public static int BELL_CHAIN_LENGTH;
        //spawns
        public static int FIREFLY_MIN;
        public static int FIREFLY_MAX;
        public static int FIREFLY_WEIGHT;
        public static List<? extends String> FIREFLY_BIOMES;
        public static List<? extends String> FIREFLY_MOD_WHITELIST;
        public static boolean FIREFLY_DESPAWN;
        //blocks
        public static List<? extends String> SIGN_POST_ADDITIONAL;
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
        public static List<? extends String> JAR_COOKIES;
        public static boolean NOTICE_BOARDS_UNRESTRICTED;
        public static List<? extends String> MOB_JAR_ALLOWED_MOBS;
        public static List<? extends String> MOB_JAR_TINTED_ALLOWED_MOBS;
        public static List<? extends String> CAGE_ALLOWED_MOBS;
        public static List<? extends String> CAGE_ALLOWED_BABY_MOBS;
        public static boolean CAGE_ALL_MOBS;
        public static List<? extends String> SACK_WHITELIST;
        public static int SACK_INCREMENT;
        public static boolean SACK_PENALTY;
        public static int SACK_SLOTS;
        public static boolean SAFE_UNBREAKABLE;
        public static int GLOBE_TRADES;
        public static double GLOBE_TREASURE_CHANCE;
        //entity
        public static int FIREFLY_PERIOD;
        public static double FIREFLY_SPEED;

        public static void refresh(){
            FLUTE_DISTANCE = item.FLUTE_DISTANCE.get();
            FLUTE_RADIUS = item.FLUTE_RADIUS.get();

            DIRECTIONAL_CAKE = tweaks.DIRECTIONAL_CAKE.get();
            DOUBLE_CAKE_PLACEMENT = tweaks.DOUBLE_CAKE_PLACEMENT.get();
            HANGING_POT_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            WALL_LANTERN_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            THROWABLE_BRICKS_ENABLED = tweaks.THROWABLE_BRICKS_ENABLED.get();
            BRICKS_LIST = tweaks.BRICKS_LIST.get();
            WALL_LANTERN_BLACKLIST = tweaks.WALL_LANTERN_BLACKLIST.get();
            BELL_CHAIN = tweaks.BELL_CHAIN.get();
            BELL_CHAIN_LENGTH = tweaks.BELL_CHAIN_LENGTH.get();

            FIREFLY_MIN = spawn.FIREFLY_MIN.get();
            FIREFLY_MAX = spawn.FIREFLY_MAX.get();
            FIREFLY_WEIGHT = spawn.FIREFLY_WEIGHT.get();
            FIREFLY_BIOMES = spawn.FIREFLY_BIOMES.get();
            FIREFLY_MOD_WHITELIST = spawn.FIREFLY_MOD_WHITELIST.get();

            GLOBE_TRADES = block.GLOBE_TRADES.get();
            GLOBE_TREASURE_CHANCE = block.GLOBE_TREASURE_CHANCHE.get();

            SIGN_POST_ADDITIONAL= block.SIGN_POST_ADDITIONAL.get();

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
            JAR_COOKIES = block.JAR_COOKIES.get();

            NOTICE_BOARDS_UNRESTRICTED = block.NOTICE_BOARDS_UNRESTRICTED.get();

            MOB_JAR_ALLOWED_MOBS = block.MOB_JAR_ALLOWED_MOBS.get();
            MOB_JAR_TINTED_ALLOWED_MOBS = block.MOB_JAR_TINTED_ALLOWED_MOBS.get();

            CAGE_ALLOWED_MOBS = block.CAGE_ALLOWED_MOBS.get();
            CAGE_ALLOWED_BABY_MOBS = block.CAGE_ALLOWED_BABY_MOBS.get();
            CAGE_ALL_MOBS = block.CAGE_ALL_MOBS.get();

            SACK_WHITELIST = block.SACK_WHITELIST.get();
            SACK_INCREMENT = block.SACK_INCREMENT.get();
            SACK_PENALTY = block.SACK_PENALTY.get();
            SACK_SLOTS = block.SACK_SLOTS.get();

            SAFE_UNBREAKABLE= block.SAFE_UNBREAKABLE.get();

            FIREFLY_PERIOD = entity.FIREFLY_PERIOD.get();
            FIREFLY_SPEED = entity.FIREFLY_SPEED.get();
            FIREFLY_DESPAWN = entity.FIREFLY_DESPAWN.get();

        }
    }
}