package net.mehvahdjukaar.supplementaries.configs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfigs {
    public static ForgeConfigSpec SERVER_CONFIG;

    static {
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        //reg.init(SERVER_BUILDER);
        block.init(SERVER_BUILDER);
        spawn.init(SERVER_BUILDER);
        entity.init(SERVER_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
    }


    public static class block {
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

        public static ForgeConfigSpec.BooleanValue WALL_LANTERN_PLACEMENT;

        public static ForgeConfigSpec.IntValue JAR_CAPACITY;
        public static ForgeConfigSpec.BooleanValue JAR_EAT;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_ALLOWED_MOBS;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_TINTED_ALLOWED_MOBS;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> CAGE_ALLOWED_MOBS;

        public static ForgeConfigSpec.BooleanValue NOTICE_BOARDS_UNRESTRICTED;

        private static void  init(ForgeConfigSpec.Builder builder){
            builder.comment("Server side blocks configs")
                    .push("blocks");

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
                    .defineList("mobs", turnTableBlacklist,s -> true);
            builder.pop();
            //wall lantern
            builder.push("wall_lantern");
            WALL_LANTERN_PLACEMENT = builder.comment("allow wall lanterns placement")
                    .define("enabled",true);
            builder.pop();
            //jar
            builder.push("jar");
            JAR_CAPACITY = builder.comment("jar liquid capacity: leave at 12 for pixel accuracy")
                    .defineInRange("capacity",12,0,1024);
            JAR_EAT = builder.comment("allow right click to instantly eat or drink food or potions inside a jar.\n" +
                    "Disable if you think this ability is op. Cookies are excluded")
                    .define("drink_from_jar",true);


            List<String> defaultMobs = Arrays.asList("minecraft:slime",
                    "minecraft:bee","minecraft:magma_cube","iceandfire:pixie","alexmobs:crimson_mosquito",
                    "mysticalworld:frog","mysticalworld:beetle", "druidcraft:lunar_moth", "druidcraft:dreadfish","swampexpansion:slabfish",
                    "savageandravage:creepie","betteranimalsplus:butterfly");
            MOB_JAR_ALLOWED_MOBS = builder.comment("catchable mobs \n"+
                    "BE VERY CAREFUL WITH THESE: SOME MOBS MIGHT NOT WORK OF EVEN CRASH THE GAME.\n"+
                    "That's due to a vanilla bug. Check in a new world if the mobs you added here work before adding to a modpack")
                    .defineList("mobs", defaultMobs,s -> true);
            List<String> defaultMobsTinted = Arrays.asList("minecraft:endermite","minecraft:slime",
                    "minecraft:bee","minecraft:magma_cube", "minecraft:vex","iceandfire:pixie","alexmobs:crimson_mosquito",
                    "mysticalworld:frog","mysticalworld:beetle", "druidcraft:lunar_moth", "druidcraft:dreadfish","swampexpansion:slabfish",
                    "savageandravage:creepie","betteranimalsplus:butterfly");
            MOB_JAR_TINTED_ALLOWED_MOBS = builder.comment("tinted jar catchable mobs")
                    .defineList("tinted_jar_mobs", defaultMobsTinted,s -> true);
            builder.pop();

            //cage
            builder.comment("I haven't tested most of the mods included here. let me know if they work")
                    .push("cage");
            List<String> defaultCageMobs = Arrays.asList("minecraft:endermite","minecraft:slime","minecraft:parrot",
                    "minecraft:bee","minecraft:magma_cube", "minecraft:vex","minecraft:rabbit", "minecraft:cat",
                    "minecraft:chicken","minecraft:bat","iceandfire:pixie","minecraft:fox","minecraft:ocelot",
                    "alexmobs:roadrunner","alexmobs:hummingbird","alexmobs:crimson_mosquito", "alexmobs:rattlesnake",
                    "alexmobs:lobster","alexmobs:capuchin_monkey","alexmobs:warped_toad","mysticalworld:beetle",
                    "mysticalworld:frog", "mysticalworld:silver_fox", "mysticalworld:sprout", "mysticalworld:endermini", "mysticalworld:lava_cat",
                    "mysticalworld:owl", "mysticalworld:silkworm", "mysticalworld:hell_sprout","quark:toretoise",
                    "quark:crab", "quark:foxhound", "quark:stoneling", "quark:frog","rats:rat", "rats:piper",
                    "rats:plague_doctor", "rats:black_death", "rats:plague_cloud", "rats:plague_beast", "rats:rat_king", "rats:demon_rat", "rats:ratlantean_spirit",
                    "rats:ratlantean_automation", "rats:feral_ratlantean", "rats:neo_ratlantean", "rats:pirat", "rats:ghost_pirat", "rats:dutchrat", "rats:ratfish",
                    "rats:ratlantean_ratbot", "rats:rat_baron", "goblintraders:goblin_trader", "goblintraders:vein_goblin_trader",
                    "autumnity:snail","buzzierbees:honey_slime", "betteranimalsplus:lammergeier","betteranimalsplus:songbird",
                    "betteranimalsplus:pheasant", "betteranimalsplus:squirrel", "betteranimalsplus:badger", "betteranimalsplus:turkey",
                    "exoticbirds:roadrunner","exoticbirds:hummingbird","exoticbirds:woodpecker","exoticbirds:kingfisher",
                    "exoticbirds:toucan","exoticbirds:macaw","exoticbirds:magpie", "exoticbirds:kiwi", "exoticbirds:owl",
                    "exoticbirds:gouldianfinch", "exoticbirds:gull", "exoticbirds:pigeon", "exoticbirds:penguin", "exoticbirds:duck",
                    "exoticbirds:booby", "exoticbirds:cardinal", "exoticbirds:bluejay", "exoticbirds:robin", "exoticbirds:kookaburra",
                    "exoticbirds:budgerigar", "exoticbirds:cockatoo","swampexpansion:slabfish");
            CAGE_ALLOWED_MOBS = builder.comment("catchable mobs")
                    .defineList("cage_mobs", defaultCageMobs,s -> true);

            builder.pop();

            //notice boards
            builder.push("notice_board");
            NOTICE_BOARDS_UNRESTRICTED = builder.comment("allow notice boards to accept and display any item, not just maps and books")
                    .define("allow_any_item", false);
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
            List<String> defaultBiomes = Arrays.asList("minecraft:swamp","minecraft:swamp_hills","minecraft:plains","minecraft:sunflower_plains","minecraft:dark_forest","minecraft:dark_forest_hills");
            List<String> defaultMods = Arrays.asList();
            //TODO add validation for biomes
            FIREFLY_BIOMES = builder.comment("Spawnable biomes")
                    .defineList("biomes", defaultBiomes, s -> true);
            FIREFLY_MOD_WHITELIST = builder.comment("Whitelisted mods. All biomes from said mods will be able to spawn fireflies. Use the one above for more control")
                    .defineList("mod_whitelist", defaultMods, s -> true);
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

            builder.pop();

            builder.pop();
        }
    }




    //maybe not need but hey
    public static class cached{
        //spawns
        public static int FIREFLY_MIN;
        public static int FIREFLY_MAX;
        public static int FIREFLY_WEIGHT;
        public static List<? extends String> FIREFLY_BIOMES;
        public static List<? extends String> FIREFLY_MOD_WHITELIST;
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
        public static boolean WALL_LANTERN_PLACEMENT;
        public static int JAR_CAPACITY;
        public static boolean JAR_EAT;
        public static boolean NOTICE_BOARDS_UNRESTRICTED;
        public static List<? extends String> MOB_JAR_ALLOWED_MOBS;
        public static List<? extends String> MOB_JAR_TINTED_ALLOWED_MOBS;
        public static List<? extends String> CAGE_ALLOWED_MOBS;
        //entity
        public static int FIREFLY_PERIOD;
        public static double FIREFLY_SPEED;

        public static void refresh(){
            FIREFLY_MIN = spawn.FIREFLY_MIN.get();
            FIREFLY_MAX = spawn.FIREFLY_MAX.get();
            FIREFLY_WEIGHT = spawn.FIREFLY_WEIGHT.get();
            FIREFLY_BIOMES = spawn.FIREFLY_BIOMES.get();
            FIREFLY_MOD_WHITELIST = spawn.FIREFLY_MOD_WHITELIST.get();

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

            WALL_LANTERN_PLACEMENT = block.WALL_LANTERN_PLACEMENT.get();

            JAR_CAPACITY = block.JAR_CAPACITY.get();

            JAR_EAT = block.JAR_EAT.get();

            NOTICE_BOARDS_UNRESTRICTED = block.NOTICE_BOARDS_UNRESTRICTED.get();

            MOB_JAR_ALLOWED_MOBS = block.MOB_JAR_ALLOWED_MOBS.get();
            MOB_JAR_TINTED_ALLOWED_MOBS = block.MOB_JAR_TINTED_ALLOWED_MOBS.get();

            CAGE_ALLOWED_MOBS = block.CAGE_ALLOWED_MOBS.get();

            FIREFLY_PERIOD = entity.FIREFLY_PERIOD.get();
            FIREFLY_SPEED = entity.FIREFLY_SPEED.get();

        }
    }

    /*
    @SubscribeEvent
    public static void loadConfig(ModConfig.Loading event) {
        if(event.getConfig().getType() == ModConfig.Type.COMMON)
            cached.refresh();
    }

    @SubscribeEvent
    public static void reloadConfig(ModConfig.Reloading event) {

        if(event.getConfig().getType() == ModConfig.Type.COMMON)
            cached.refresh();
    }*/

    @SubscribeEvent
    public static void configEvent(ModConfig.ModConfigEvent event) {
        if(event.getConfig().getSpec() == SERVER_CONFIG)
            cached.refresh();
        else if(event.getConfig().getSpec() == ClientConfigs.CLIENT_CONFIG)
            ClientConfigs.cached.refresh();
    }

}