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

        public static ForgeConfigSpec.BooleanValue WALL_LANTERN_PLACEMENT;

        public static ForgeConfigSpec.IntValue JAR_CAPACITY;

        public static ForgeConfigSpec.BooleanValue NOTICE_BOARDS_UNRESTRICTED;

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_ALLOWED_MOBS;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_JAR_TINTED_ALLOWED_MOBS;

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
            TURN_TABLE_PERIOD = builder.comment("how many ticks it takes to rotate a block/entity")
                    .defineInRange("period", 20, 1, 256);
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

            List<String> defaultMobs = Arrays.asList("minecraft:slime","minecraft:parrot",
                    "minecraft:bee","minecraft:magma_cube");
            MOB_JAR_ALLOWED_MOBS = builder.comment("catchable mobs")
                    .defineList("mobs", defaultMobs,s -> true);
            List<String> defaultMobsTinted = Arrays.asList("minecraft:endermite","minecraft:slime","minecraft:parrot",
                    "minecraft:bee","minecraft:magma_cube", "minecraft:vex");
            MOB_JAR_TINTED_ALLOWED_MOBS = builder.comment("tinted jar catchable mobs")
                    .defineList("tinted_jar_mobs", defaultMobsTinted,s -> true);
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

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Configure spawning conditions")
                    .push("spawns");
            builder.push("firefly");
            List<String> defaultBiomes = Arrays.asList("minecraft:swamp","minecraft:swamp_hills","minecraft:plains","minecraft:sunflower_plains","minecraft:dark_forest","minecraft:dark_forest_hills");
            //TODO add validation for biomes
            FIREFLY_BIOMES = builder.comment("Spawnable biomes")
                    .defineList("biomes", defaultBiomes, s -> true);
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

    public static class reg {
        public static ForgeConfigSpec.BooleanValue FIREFLY_ENABLED;
        public static ForgeConfigSpec.BooleanValue PLANTER_ENABLED;
        public static ForgeConfigSpec.BooleanValue CLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue PEDESTAL_ENABLED;
        public static ForgeConfigSpec.BooleanValue WIND_VANE_ENABLED;
        public static ForgeConfigSpec.BooleanValue ILLUMINATOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue NOTICE_BOARD_ENABLED;
        public static ForgeConfigSpec.BooleanValue CRANK_ENABLED;
        public static ForgeConfigSpec.BooleanValue JAR_ENABLED;
        public static ForgeConfigSpec.BooleanValue FAUCET_ENABLED;
        public static ForgeConfigSpec.BooleanValue TURN_TABLE_ENABLED;
        public static ForgeConfigSpec.BooleanValue PISTON_LAUNCHER_ENABLED;
        public static ForgeConfigSpec.BooleanValue SPEAKER_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue SIGN_POST_ENABLED;
        public static ForgeConfigSpec.BooleanValue HANGING_SIGN_ENABLED;
        public static ForgeConfigSpec.BooleanValue WALL_LANTERN_ENABLED;
        public static ForgeConfigSpec.BooleanValue BELLOWS_ENABLED;
        //public static ForgeConfigSpec.BooleanValue SCONCE_ENABLED;
        public static ForgeConfigSpec.BooleanValue CANDELABRA_ENABLED;
        public static ForgeConfigSpec.BooleanValue LASER_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLAG_ENABLED;

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("CURRENTLY NOT WORKING");
            builder.comment("if you know how to load forge configs before registration to allow this let me know pls");
            builder.comment("Enable and disable blocks / entities");
            builder.push("registration");
            builder.push("blocks");
            PLANTER_ENABLED = builder.define("planter", true);
            CLOCK_ENABLED = builder.define("clock_block", true);
            PEDESTAL_ENABLED = builder.define("pedestal", true);
            WIND_VANE_ENABLED = builder.define("wind_vane", true);
            ILLUMINATOR_ENABLED = builder.define("redstone_illuminator", true);
            NOTICE_BOARD_ENABLED = builder.define("notice_board", true);
            CRANK_ENABLED = builder.define("crank", true);
            JAR_ENABLED = builder.define("jar", true);
            FAUCET_ENABLED = builder.define("faucet", true);
            TURN_TABLE_ENABLED = builder.define("turn_table", true);
            PISTON_LAUNCHER_ENABLED = builder.define("spring_launcher", true);
            SPEAKER_BLOCK_ENABLED = builder.define("speaker_block", true);
            SIGN_POST_ENABLED = builder.define("sign_post", true);
            HANGING_SIGN_ENABLED = builder.define("hanging_sign", true);
            WALL_LANTERN_ENABLED = builder.define("wall_lantern", true);
            BELLOWS_ENABLED = builder.define("bellows", true);
            //SCONCE_ENABLED = builder.define("sconce", true);
            CANDELABRA_ENABLED = builder.define("candelabra", true);
            builder.comment("WIP");
            LASER_ENABLED = builder.define("laser", true);
            builder.comment("WIP");
            FLAG_ENABLED = builder.define("flag", true);
            builder.pop();
            builder.push("entities");
            FIREFLY_ENABLED = builder.define("firefly", true);
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
        public static boolean WALL_LANTERN_PLACEMENT;
        public static int JAR_CAPACITY;
        public static boolean NOTICE_BOARDS_UNRESTRICTED;
        public static List<? extends String> MOB_JAR_ALLOWED_MOBS;
        public static List<? extends String> MOB_JAR_TINTED_ALLOWED_MOBS;

        public static void refresh(){
            FIREFLY_MIN = spawn.FIREFLY_MIN.get();
            FIREFLY_MAX = spawn.FIREFLY_MAX.get();
            FIREFLY_WEIGHT = spawn.FIREFLY_WEIGHT.get();
            FIREFLY_BIOMES = spawn.FIREFLY_BIOMES.get();

            SPEAKER_RANGE = block.SPEAKER_RANGE.get();

            BELLOWS_PERIOD = block.BELLOWS_PERIOD.get();
            BELLOWS_POWER_SCALING = block.BELLOWS_POWER_SCALING.get();
            BELLOWS_MAX_VEL = block.BELLOWS_MAX_VEL.get();
            BELLOWS_BASE_VEL_SCALING = block.BELLOWS_BASE_VEL_SCALING.get();
            BELLOWS_FLAG = block.BELLOWS_FLAG.get();
            BELLOWS_RANGE = block.BELLOWS_RANGE.get();

            LAUNCHER_VEL = block.LAUNCHER_VEL.get();
            LAUNCHER_HEIGHT = block.LAUNCHER_HEIGHT.get();

            TURN_TABLE_PERIOD = block.TURN_TABLE_PERIOD.get();

            WALL_LANTERN_PLACEMENT = block.WALL_LANTERN_PLACEMENT.get();

            JAR_CAPACITY = block.JAR_CAPACITY.get();

            NOTICE_BOARDS_UNRESTRICTED = block.NOTICE_BOARDS_UNRESTRICTED.get();

            MOB_JAR_ALLOWED_MOBS = block.MOB_JAR_ALLOWED_MOBS.get();
            MOB_JAR_TINTED_ALLOWED_MOBS = block.MOB_JAR_TINTED_ALLOWED_MOBS.get();

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