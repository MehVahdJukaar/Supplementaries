package net.mehvahdjukaar.supplementaries.configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServerConfigs {


    //overwritten by server one
    public static ForgeConfigSpec SERVER_CONFIG;

    static {
        createConfig();
    }


    public static void createConfig(){
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        //reg.init(SERVER_BUILDER);

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
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> FLUTE_EXTRA_MOBS;


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
            FLUTE_EXTRA_MOBS = builder.comment("additional non tameable entities that you can bind to flutes")
                    .defineList("flute_extra_mobs", Arrays.asList("minecraft:horse","minecraft:llama","minecraft:mule","minecraft:donkey","minecraft:fox"),s->true);

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

            List<String> modBlacklist = Arrays.asList("extlights","betterendforge");
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

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_ALLOWED_MOBS;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_TINTED_ALLOWED_MOBS;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> CAGE_ALLOWED_MOBS;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> CAGE_ALLOWED_BABY_MOBS;
        public static ForgeConfigSpec.BooleanValue CAGE_ALL_MOBS;
        public static ForgeConfigSpec.BooleanValue CAGE_ALL_BABIES;

        public static ForgeConfigSpec.BooleanValue NOTICE_BOARDS_UNRESTRICTED;

        public static ForgeConfigSpec.BooleanValue SACK_PENALTY;
        public static ForgeConfigSpec.IntValue SACK_INCREMENT;
        public static ForgeConfigSpec.IntValue SACK_SLOTS;

        public static ForgeConfigSpec.BooleanValue SAFE_UNBREAKABLE;
        public static ForgeConfigSpec.BooleanValue SAFE_SIMPLE;

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

            List<String> jarMobs = Arrays.asList("minecraft:slime", "minecraft:bee","minecraft:magma_cube",
                    "iceandfire:pixie","alexsmobs:fly", "alexsmobs:hummingbird","alexsmobs:cockroach","terraincognita:butterfly",
                    "buzzierbees:honey_slime", "mysticalworld:frog","mysticalworld:beetle","mysticalworld:silkworm",
                    "druidcraft:lunar_moth", "druidcraft:dreadfish","swampexpansion:slabfish","betteranimalsplus:goose",
                    "endergetic:puff_bug", "betterendforge:end_slime", "betterendforge:dragonfly", "betterendforge:silk_moth",
                    "savageandravage:creepie","betteranimalsplus:butterfly","whisperwoods:moth","fins:river_pebble_snail");
            List<String> jarMobsAndFishes = new ArrayList<>(jarMobs);
            jarMobsAndFishes.addAll(CapturedMobs.getFishes());
            MOB_JAR_ALLOWED_MOBS = builder.comment("catchable mobs \n"+
                    "due to a vanilla bug some mobs might not render correctly or at all")
                    .defineList("mobs", jarMobsAndFishes,s -> true);
            List<String> tintedMobs = new ArrayList<>(jarMobs);
            tintedMobs.addAll(Arrays.asList("minecraft:endermite","minecraft:vex","alexsmobs:mimicube"));
            List<String> tintedMobsAndFishes = new ArrayList<>(tintedMobs);
            tintedMobsAndFishes.addAll(CapturedMobs.getFishes());
            MOB_JAR_TINTED_ALLOWED_MOBS = builder.comment("tinted jar catchable mobs")
                    .defineList("tinted_jar_mobs", tintedMobsAndFishes,s -> true);
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
                    "exoticbirds:budgerigar", "exoticbirds:cockatoo","swampexpansion:slabfish",
                    "betteranimalsplus:horseshoecrab", "betteranimalsplus:crab", "whisperwoods:wisp",
                    "undergarden:muncher", "undergarden:scintling", "undergarden:rotling",  "undergarden:sploogie",
                    "dungeonsmod:crow", "dungeonsmod:anthermite", "pandoras_creatures:crab",
                    "environmental:duck", "environmental:cardinal", "environmental:fennec_fox", "environmental:slabfish", "environmental:penguin",
                    "fins:flatback_leaf_snail","fins:penglil", "fins:river_pebble_snail", "fins:siderol_whiskered_snail", "fins:red_bull_crab", "fins:white_bull_crab");
            cageMobs.addAll(additionalCageMobs);
            CAGE_ALLOWED_MOBS = builder.comment("catchable mobs")
                    .defineList("cage_mobs", cageMobs,s -> true);
            List<String> cageBabyMobs = Arrays.asList("minecraft:cow","minecraft:sheep","minecraft:pig","alexsmobs:crocodile", "alexsmobs:endergrade", "alexsmobs:gazelle", "alexsmobs:gorilla", "alexsmobs:komodo_dragon", "alexsmobs:raccoon", "alexsmobs:seal", "alexsmobs:warped_toad");
            CAGE_ALLOWED_BABY_MOBS = builder.comment("additional mobs that you'll be able to catch with the added condition that it has to be a baby variant. No need to include the ones already in cage_mobs")
                    .defineList("cage_baby_mobs", cageBabyMobs,s -> true);
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
            SACK_PENALTY = builder.comment("penalize the player with slowness effect when carrying too many sacks")
                    .define("sack_penality", true);
            SACK_INCREMENT = builder.comment("maximum number of sacks after which the slowness effect will be applied. each multiple of this number will further slow the player down")
                    .defineInRange("sack_increment",2,0,50);
            SACK_SLOTS = builder.comment("additional sack slots divided by 2. Number of slots will be 5 + additional_slots*2")
                    .defineInRange("additional_slots",1,0,2);
            builder.pop();

            builder.push("safe");
            SAFE_UNBREAKABLE = builder.comment("makes safes only breakable by their owner or by a player in creative")
                    .define("prevent_breaking",false);
            SAFE_SIMPLE = builder.comment("make safes simpler so they do not require keys:\n" +
                    "they will be bound to the first person that opens one and only that person will be able to interact with them")
                    .define("simple_safes",false);
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

        public static ForgeConfigSpec.IntValue ROAD_SIGN_SPAWN_CHANCE;
        public static ForgeConfigSpec.BooleanValue EXPERIMENTAL_ROAD_SIGN;

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
            EXPERIMENTAL_ROAD_SIGN = builder.comment("enable experimental road sign structures. This will likely change in the nar future")
                    .define("experimental_road_signs",true);
            ROAD_SIGN_SPAWN_CHANCE = builder.comment("the higher this number is the less sign posts you'll find")
                    .defineInRange("rarity",80,1,2000);
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
        public static List<? extends String> FLUTE_EXTRA_MOBS;
        //tweaks
        public static boolean DIRECTIONAL_CAKE;
        public static boolean DOUBLE_CAKE_PLACEMENT;
        public static boolean HANGING_POT_PLACEMENT;
        public static boolean THROWABLE_BRICKS_ENABLED;
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
        public static List<? extends String> MOB_JAR_ALLOWED_MOBS;
        public static List<? extends String> MOB_JAR_TINTED_ALLOWED_MOBS;
        public static List<? extends String> CAGE_ALLOWED_MOBS;
        public static List<? extends String> CAGE_ALLOWED_BABY_MOBS;
        public static boolean CAGE_ALL_MOBS;
        public static boolean CAGE_ALL_BABIES;
        public static int SACK_INCREMENT;
        public static boolean SACK_PENALTY;
        public static int SACK_SLOTS;
        public static boolean SAFE_UNBREAKABLE;
        public static boolean SAFE_SIMPLE;
        public static int GLOBE_TRADES;
        public static double GLOBE_TREASURE_CHANCE;
        //entity
        public static int FIREFLY_PERIOD;
        public static double FIREFLY_SPEED;

        public static void refresh(){
            ROPE_ARROW_ROPE = item.ROPE_ARROW_ROPE.get();
            ROPE_ARROW_BLOCK = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ROPE_ARROW_ROPE));
            if(ROPE_ARROW_BLOCK == Blocks.AIR)ROPE_ARROW_BLOCK = Registry.ROPE.get();
            FLUTE_DISTANCE = item.FLUTE_DISTANCE.get();
            FLUTE_RADIUS = item.FLUTE_RADIUS.get();
            FLUTE_EXTRA_MOBS = item.FLUTE_EXTRA_MOBS.get();

            DIRECTIONAL_CAKE = tweaks.DIRECTIONAL_CAKE.get();
            DOUBLE_CAKE_PLACEMENT = tweaks.DOUBLE_CAKE_PLACEMENT.get();
            HANGING_POT_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            WALL_LANTERN_PLACEMENT = tweaks.WALL_LANTERN_PLACEMENT.get();
            THROWABLE_BRICKS_ENABLED = tweaks.THROWABLE_BRICKS_ENABLED.get();
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

            MOB_JAR_ALLOWED_MOBS = block.MOB_JAR_ALLOWED_MOBS.get();
            MOB_JAR_TINTED_ALLOWED_MOBS = block.MOB_JAR_TINTED_ALLOWED_MOBS.get();

            CAGE_ALLOWED_MOBS = block.CAGE_ALLOWED_MOBS.get();
            CAGE_ALLOWED_BABY_MOBS = block.CAGE_ALLOWED_BABY_MOBS.get();
            CAGE_ALL_MOBS = block.CAGE_ALL_MOBS.get();
            CAGE_ALL_BABIES = block.CAGE_ALL_BABIES.get();

            SACK_INCREMENT = block.SACK_INCREMENT.get();
            SACK_PENALTY = block.SACK_PENALTY.get();
            SACK_SLOTS = block.SACK_SLOTS.get();

            SAFE_UNBREAKABLE= block.SAFE_UNBREAKABLE.get();
            SAFE_SIMPLE = block.SAFE_SIMPLE.get();

            FIREFLY_PERIOD = entity.FIREFLY_PERIOD.get();
            FIREFLY_SPEED = entity.FIREFLY_SPEED.get();
            FIREFLY_DESPAWN = entity.FIREFLY_DESPAWN.get();

        }
    }
}