package net.mehvahdjukaar.supplementaries.configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
//loaded before registry
public class RegistryConfigs {
    public static ForgeConfigSpec REGISTRY_CONFIG;

    static {
        ForgeConfigSpec.Builder REGISTRY_BUILDER = new ForgeConfigSpec.Builder();

        reg.init(REGISTRY_BUILDER);

        REGISTRY_CONFIG = REGISTRY_BUILDER.build();

    }
    public static void registerConfig(){
        CommentedFileConfig replacementConfig = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(Supplementaries.MOD_ID + "-registry.toml"))
                .sync()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        replacementConfig.load();
        replacementConfig.save();
        REGISTRY_CONFIG.setConfig(replacementConfig);
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
        public static ForgeConfigSpec.BooleanValue SCONCE_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_ENDER_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_GREEN_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_SOUL_ENABLED;
        public static ForgeConfigSpec.BooleanValue CANDELABRA_ENABLED;
        public static ForgeConfigSpec.BooleanValue CANDELABRA_SILVER_ENABLED;
        public static ForgeConfigSpec.BooleanValue CAGE_ENABLED;
        public static ForgeConfigSpec.BooleanValue ITEM_SHELF_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_LEVER_ENABLED;
        public static ForgeConfigSpec.BooleanValue CANDLE_HOLDER_ENABLED;
        public static ForgeConfigSpec.BooleanValue COG_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue STONE_LAMP_ENABLED;
        public static ForgeConfigSpec.BooleanValue LASER_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLAG_ENABLED;

        private static void init(ForgeConfigSpec.Builder builder) {
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
            //WALL_LANTERN_ENABLED = builder.define("wall_lantern", true);
            BELLOWS_ENABLED = builder.define("bellows", true);
            SCONCE_ENABLED = builder.define("sconce", true);
            SCONCE_GREEN_ENABLED = builder.define("sconce_green", true);
            SCONCE_ENDER_ENABLED = builder.define("sconce_ender", true);
            SCONCE_SOUL_ENABLED = builder.define("sconce_soul", true);
            CANDELABRA_ENABLED = builder.define("candelabra", true);
            CANDELABRA_SILVER_ENABLED = builder.define("candelabra_silver", true);
            CAGE_ENABLED = builder.define("mob_cage", true);
            ITEM_SHELF_ENABLED = builder.define("item_shelf", true);
            SCONCE_LEVER_ENABLED = builder.define("sconce_lever", true);
            COG_BLOCK_ENABLED = builder.define("sconce_lever", true);
            STONE_LAMP_ENABLED = builder.define("sconce_lever", true);
            CANDLE_HOLDER_ENABLED = builder.define("sconce_lever", true);

            LASER_ENABLED = builder.comment("WIP")
                    .define("laser", true);
            FLAG_ENABLED = builder.comment("WIP")
                    .define("flag", true);
            builder.pop();

            builder.push("entities");
            FIREFLY_ENABLED = builder.define("firefly", true);
            builder.pop();

            builder.pop();

        }


    }

}