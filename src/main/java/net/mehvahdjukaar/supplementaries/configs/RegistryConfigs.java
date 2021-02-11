package net.mehvahdjukaar.supplementaries.configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
//loaded before registry
public class RegistryConfigs {
    public static ForgeConfigSpec REGISTRY_CONFIG;

    static {
        ForgeConfigSpec.Builder REGISTRY_BUILDER = new ForgeConfigSpec.Builder();
        //TODO: see how block carpentry mod extrapolates textures from items to use on hourglass
        reg.init(REGISTRY_BUILDER);

        REGISTRY_CONFIG = REGISTRY_BUILDER.build();
        //remove try?
        try{
            if(ModList.get().isLoaded("mysticalworld")||ModList.get().isLoaded("immersiveengineering")||
                    ModList.get().isLoaded("bluepower")||ModList.get().isLoaded("silents_mechanisms ")||
                    ModList.get().isLoaded("thermal")||ModList.get().isLoaded("iceandfire")
                    ||ModList.get().isLoaded("silentgems")||ModList.get().isLoaded("occultism")){
                reg.HAS_SILVER=true;
            }

            reg.FIREFLY_JAR = reg.FIREFLY_ENABLED.get() && reg.JAR_ENABLED.get();
            reg.SILVER_CANDELABRA = reg.CANDELABRA_ENABLED.get() && reg.HAS_SILVER;
        }
        catch(Exception ignored){};


    }
    public static void load(){
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
        public static ForgeConfigSpec.BooleanValue BELLOWS_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_ENDER_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_GREEN_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_SOUL_ENABLED;
        public static ForgeConfigSpec.BooleanValue CANDELABRA_ENABLED;
        public static ForgeConfigSpec.BooleanValue CAGE_ENABLED;
        public static ForgeConfigSpec.BooleanValue ITEM_SHELF_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_LEVER_ENABLED;
        public static ForgeConfigSpec.BooleanValue CANDLE_HOLDER_ENABLED;
        public static ForgeConfigSpec.BooleanValue COG_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue STONE_LAMP_ENABLED;
        public static ForgeConfigSpec.BooleanValue GLOBE_ENABLED;
        public static ForgeConfigSpec.BooleanValue HOURGLASS_ENABLED;
        public static ForgeConfigSpec.BooleanValue LASER_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLAG_ENABLED;
        public static ForgeConfigSpec.BooleanValue SACK_ENABLED;
        public static ForgeConfigSpec.BooleanValue BLACKBOARD_ENABLED;
        public static ForgeConfigSpec.BooleanValue SAFE_ENABLED;
        public static ForgeConfigSpec.BooleanValue COPPER_LANTERN_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLUTE_ENABLED;
        public static ForgeConfigSpec.BooleanValue GOLD_TRAPDOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue GOLD_DOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue BAMBOO_SPIKES_ENABLED;

        public static ForgeConfigSpec.BooleanValue CREATIVE_TAB;
        public static ForgeConfigSpec.BooleanValue DISPENSERS;

        public static boolean FIREFLY_JAR = true;
        public static boolean SILVER_CANDELABRA = false;
        public static boolean HAS_COPPER = false;
        public static boolean HAS_SILVER = false;


        //oh god what have I done
        public static boolean isEnabled(String path){
            //special double condition cases
            if(path.equals(Registry.FIREFLY_JAR_NAME)){
                return reg.FIREFLY_JAR;
            }
            if(path.equals(Registry.CANDELABRA_NAME_SILVER)){
                return reg.SILVER_CANDELABRA;
            }
            if(path.equals(Registry.SOUL_JAR_NAME)){
                return reg.JAR_ENABLED.get();
            }
            for (Field f : reg.class.getDeclaredFields()) {
                try{
                    if(ForgeConfigSpec.BooleanValue.class.isAssignableFrom(f.getType())){
                        ForgeConfigSpec.BooleanValue b = (ForgeConfigSpec.BooleanValue) f.get(null);
                        String p = b.getPath().get(b.getPath().size()-1);
                        if(p.equals(path))return b.get();
                    }
                } catch (Exception ignored) {}
            }
            return true;
        }

        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("all these don't actually disable blocks anymore, they just remove their recipe and remove them from the creative tabs(like all other mods do)\n"+
                    "to access server configuration go into /saves/serverconfigs")
                    .push("general");
            CREATIVE_TAB = builder.comment("enable creative tab").define("creative_tab",false);

            DISPENSERS = builder.comment("set to false to disable custom dispenser behaviors (filling jars) if for some reason they are causing trouble").define("dispensers",true);

            builder.pop();

            builder.comment("Enable and disable blocks / entities");

            builder.push("registration");

            builder.push("blocks");
            PLANTER_ENABLED = builder.define(Registry.PLANTER_NAME, true);
            CLOCK_ENABLED = builder.define(Registry.CLOCK_BLOCK_NAME, true);
            PEDESTAL_ENABLED = builder.define(Registry.PEDESTAL_NAME, true);
            WIND_VANE_ENABLED = builder.define(Registry.WIND_VANE_NAME, true);
            ILLUMINATOR_ENABLED = builder.define(Registry.REDSTONE_ILLUMINATOR_NAME, true);
            NOTICE_BOARD_ENABLED = builder.define(Registry.NOTICE_BOARD_NAME, true);
            CRANK_ENABLED = builder.define(Registry.CRANK_NAME, true);
            JAR_ENABLED = builder.define(Registry.JAR_NAME, true);
            FAUCET_ENABLED = builder.define(Registry.FAUCET_NAME, true);
            TURN_TABLE_ENABLED = builder.define(Registry.TURN_TABLE_NAME, true);
            PISTON_LAUNCHER_ENABLED = builder.define(Registry.PISTON_LAUNCHER_NAME, true);
            SPEAKER_BLOCK_ENABLED = builder.define(Registry.SPEAKER_BLOCK_NAME, true);
            SIGN_POST_ENABLED = builder.define(Registry.SIGN_POST_NAME, true);
            HANGING_SIGN_ENABLED = builder.define(Registry.HANGING_SIGN_NAME, true);
            //WALL_LANTERN_ENABLED = builder.define("wall_lantern", true);
            BELLOWS_ENABLED = builder.define(Registry.BELLOWS_NAME, true);
            SCONCE_ENABLED = builder.define(Registry.SCONCE_NAME, true);
            SCONCE_GREEN_ENABLED = builder.define(Registry.SCONCE_NAME_GREEN, false);
            SCONCE_ENDER_ENABLED = builder.define(Registry.SCONCE_NAME_ENDER, true);
            SCONCE_SOUL_ENABLED = builder.define(Registry.SCONCE_NAME_SOUL, true);
            CANDELABRA_ENABLED = builder.define(Registry.CANDELABRA_NAME, true);
            CAGE_ENABLED = builder.define(Registry.CAGE_NAME, true);
            ITEM_SHELF_ENABLED = builder.define(Registry.ITEM_SHELF_NAME, true);
            SCONCE_LEVER_ENABLED = builder.define(Registry.SCONCE_LEVER_NAME, true);
            COG_BLOCK_ENABLED = builder.define(Registry.COG_BLOCK_NAME, true);
            CANDLE_HOLDER_ENABLED = builder.define(Registry.CANDLE_HOLDER_NAME, true);
            GLOBE_ENABLED = builder.define(Registry.GLOBE_NAME, true);
            HOURGLASS_ENABLED = builder.define(Registry.HOURGLASS_NAME, true);
            SACK_ENABLED = builder.define(Registry.SACK_NAME, true);
            BLACKBOARD_ENABLED = builder.define(Registry.BLACKBOARD_NAME, true);
            SAFE_ENABLED = builder.define(Registry.SAFE_NAME, true);
            COPPER_LANTERN_ENABLED = builder.define(Registry.COPPER_LANTERN_NAME, true);
            FLUTE_ENABLED = builder.define(Registry.FLUTE_NAME, true);
            GOLD_TRAPDOOR_ENABLED = builder.define(Registry.GOLD_TRAPDOOR_NAME,true);
            GOLD_DOOR_ENABLED = builder.define(Registry.GOLD_DOOR_NAME,true);
            BAMBOO_SPIKES_ENABLED = builder.define(Registry.BAMBOO_SPIKES_NAME,true);
            STONE_LAMP_ENABLED = builder.define(Registry.STONE_LAMP_NAME, true);

            LASER_ENABLED = builder.comment("WIP")
                    .define(Registry.LASER_NAME, false);
            FLAG_ENABLED = builder.comment("WIP")
                    .define(Registry.FLAG_NAME, false);
            builder.pop();

            builder.push("entities");
            FIREFLY_ENABLED = builder.define(Registry.FIREFLY_NAME, true);
            builder.pop();

            builder.pop();

        }


    }

}