package net.mehvahdjukaar.supplementaries.configs;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ServerConfigs {
    public static ForgeConfigSpec SERVER_CONFIG;

    static {
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        //reg.init(SERVER_BUILDER);
        spawn.init(SERVER_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();

    }


    public static class spawn {
        public static ForgeConfigSpec.IntValue FIREFLY_MIN;
        public static ForgeConfigSpec.IntValue FIREFLY_MAX;
        public static ForgeConfigSpec.IntValue FIREFLY_WEIGHT;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> FIREFLY_BIOMES;

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Configure fireflies spawn conditions");
            builder.push("spawns");
            builder.comment("Minimum group size");
            FIREFLY_MIN = builder.defineInRange("min", 4, 0, 64);
            builder.comment("Maximum group size");
            FIREFLY_MAX = builder.defineInRange("max", 7, 0, 64);
            builder.comment("Spawn weight");
            FIREFLY_WEIGHT = builder.defineInRange("weight", 2, 0, 100);
            builder.comment("Spawnable biomes");
            List<String> defaultBiomes = Arrays.asList("plains","swamp","sunflower_plains","dark_forest","dark_forest_hills");
            //TODO add validation for biomes
            FIREFLY_BIOMES = builder.defineList("biomes", defaultBiomes, s -> true);
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
}