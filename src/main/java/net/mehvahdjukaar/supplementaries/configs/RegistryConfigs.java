package net.mehvahdjukaar.supplementaries.configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.mixins.MixinConfigs;
import net.mehvahdjukaar.supplementaries.setup.RegistryConstants;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//loaded before registry
public class RegistryConfigs {

    public static final String FILE_NAME = Supplementaries.MOD_ID + "-registry.toml";
    public static ForgeConfigSpec REGISTRY_CONFIG;

    public static void createSpec() {
        ForgeConfigSpec.Builder REGISTRY_BUILDER = new ForgeConfigSpec.Builder();
        Reg.init(REGISTRY_BUILDER);
        REGISTRY_CONFIG = REGISTRY_BUILDER.build();
    }

    //TODO: maybe merge with common
    //called from mixin config so they can be accessed super early
    public static void load() {
        CommentedFileConfig replacementConfig = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(FILE_NAME))
                .sync()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        replacementConfig.load();
        replacementConfig.save();
        REGISTRY_CONFIG.setConfig(replacementConfig);

        Reg.HAS_MINESHAFT_LANTERN = Reg.COPPER_LANTERN_ENABLED.get();
        Reg.HAS_STRONGHOLD_SCONCE = Reg.SCONCE_ENABLED.get();
    }

    public static class Reg {
        public static ForgeConfigSpec.BooleanValue ASH_ENABLED;
        public static ForgeConfigSpec.BooleanValue ASH_BRICKS_ENABLED;
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
        public static ForgeConfigSpec.BooleanValue SCONCE_GREEN_ENABLED;
        public static ForgeConfigSpec.BooleanValue CAGE_ENABLED;
        public static ForgeConfigSpec.BooleanValue ITEM_SHELF_ENABLED;
        public static ForgeConfigSpec.BooleanValue SCONCE_LEVER_ENABLED;
        public static ForgeConfigSpec.BooleanValue COG_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue STONE_LAMP_ENABLED;
        public static ForgeConfigSpec.BooleanValue END_STONE_LAMP_ENABLED;
        public static ForgeConfigSpec.BooleanValue BLACKSTONE_LAMP_ENABLED;
        public static ForgeConfigSpec.BooleanValue DEEPSLATE_LAMP_ENABLED;
        public static ForgeConfigSpec.BooleanValue GLOBE_ENABLED;
        public static ForgeConfigSpec.BooleanValue HOURGLASS_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLAG_ENABLED;
        public static ForgeConfigSpec.BooleanValue SACK_ENABLED;
        public static ForgeConfigSpec.BooleanValue BLACKBOARD_ENABLED;
        public static ForgeConfigSpec.BooleanValue SAFE_ENABLED;
        public static ForgeConfigSpec.BooleanValue COPPER_LANTERN_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLUTE_ENABLED;
        public static ForgeConfigSpec.BooleanValue GOLD_TRAPDOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue GOLD_DOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue BAMBOO_SPIKES_ENABLED;
        public static ForgeConfigSpec.BooleanValue TIPPED_SPIKES_ENABLED;
        public static ForgeConfigSpec.BooleanValue CHECKERBOARD_ENABLED;
        public static ForgeConfigSpec.BooleanValue NETHERITE_TRAPDOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue NETHERITE_DOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue PANCAKES_ENABLED;
        public static ForgeConfigSpec.BooleanValue LOCK_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLAX_ENABLED;
        public static ForgeConfigSpec.BooleanValue ROPE_ENABLED;
        public static ForgeConfigSpec.BooleanValue ROPE_ARROW_ENABLED;
        public static ForgeConfigSpec.BooleanValue PULLEY_ENABLED;
        public static ForgeConfigSpec.BooleanValue FODDER_ENABLED;
        public static ForgeConfigSpec.BooleanValue BOMB_ENABLED;
        public static ForgeConfigSpec.BooleanValue MAGMA_CREAM_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue CRIMSON_LANTERN_ENABLED;
        public static ForgeConfigSpec.BooleanValue DAUB_ENABLED;
        public static ForgeConfigSpec.BooleanValue WATTLE_AND_DAUB_ENABLED;
        public static ForgeConfigSpec.BooleanValue TIMBER_FRAME_ENABLED;
        public static ForgeConfigSpec.BooleanValue TILE_ENABLED;
        public static ForgeConfigSpec.BooleanValue GOBLET_ENABLED;
        public static ForgeConfigSpec.BooleanValue RAKED_GRAVEL_ENABLED;
        public static ForgeConfigSpec.BooleanValue STATUE_ENABLED;
        public static ForgeConfigSpec.BooleanValue IRON_GATE_ENABLED;
        public static ForgeConfigSpec.BooleanValue FEATHER_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLINT_BLOCK_ENABLED;
        public static ForgeConfigSpec.BooleanValue SLINGSHOT_ENABLED;
        public static ForgeConfigSpec.BooleanValue SHULKER_HELMET_ENABLED;
        public static ForgeConfigSpec.BooleanValue CANDY_ENABLED;
        public static ForgeConfigSpec.BooleanValue WRENCH_ENABLED;
        public static ForgeConfigSpec.BooleanValue URN_ENABLED;
        public static ForgeConfigSpec.BooleanValue ANTIQUE_INK_ENABLED;
        public static ForgeConfigSpec.BooleanValue DOORMAT_ENABLED;
        public static ForgeConfigSpec.BooleanValue FLOWER_BOX_ENABLED;
        public static ForgeConfigSpec.BooleanValue BLACKSTONE_TILE_ENABLED;
        public static ForgeConfigSpec.BooleanValue SOAP_ENABLED;
        public static ForgeConfigSpec.BooleanValue BUBBLE_BLOWER_ENABLED;
        public static ForgeConfigSpec.BooleanValue GLOBE_SEPIA;
        public static ForgeConfigSpec.BooleanValue PRESENT_ENABLED;
        public static ForgeConfigSpec.BooleanValue STASIS_ENABLED;
        public static ForgeConfigSpec.BooleanValue SILVER_TRAPDOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue SILVER_DOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue LEAD_TRAPDOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue LEAD_DOOR_ENABLED;
        public static ForgeConfigSpec.BooleanValue DISPENSER_MINECART_ENABLED;

        public static ForgeConfigSpec.BooleanValue JAR_TAB;
        public static ForgeConfigSpec.BooleanValue CREATIVE_TAB;
        public static ForgeConfigSpec.BooleanValue DISPENSERS;
        public static ForgeConfigSpec.BooleanValue CUSTOM_CONFIGURED_SCREEN;
        public static ForgeConfigSpec.BooleanValue DEBUG_RESOURCES;

        public static boolean HAS_MINESHAFT_LANTERN = false;
        public static boolean HAS_STRONGHOLD_SCONCE = false;

        public static final Map<String, ForgeConfigSpec.BooleanValue> MIXIN_VALUES = new HashMap<>();

        //oh god what have I done
        public static boolean isEnabled(String path) {

            switch (path) {
                case "vertical_slabs":
                    return CompatHandler.quark && QuarkPlugin.isVerticalSlabEnabled();
                case RegistryConstants.GLOBE_SEPIA_NAME:
                    return GLOBE_SEPIA.get() && ANTIQUE_INK_ENABLED.get();
                case RegistryConstants.FLAX_WILD_NAME:
                    return FLAX_ENABLED.get();
                case RegistryConstants.KEY_NAME:
                    return NETHERITE_DOOR_ENABLED.get() || NETHERITE_TRAPDOOR_ENABLED.get() || SAFE_ENABLED.get();
            }
            for (Field f : Reg.class.getDeclaredFields()) {
                try {
                    if (ForgeConfigSpec.BooleanValue.class.isAssignableFrom(f.getType())) {
                        ForgeConfigSpec.BooleanValue b = (ForgeConfigSpec.BooleanValue) f.get(null);
                        String p = b.getPath().get(b.getPath().size() - 1);
                        if (p.equals(path)) return b.get();
                    }
                } catch (Exception ignored) {
                }
            }
            return true;
        }

        private static void init(ForgeConfigSpec.Builder builder) {

            builder.comment("Here are configs that need reloading to take effect");

            builder.push("general");
            CREATIVE_TAB = builder.comment("Enable Creative Tab").define("creative_tab", false);

            DISPENSERS = builder.comment("Set to false to disable custom dispenser behaviors (i.e: filling jars) if for some reason they are causing trouble").define("dispensers", true);

            JAR_TAB = builder.comment("Creates a creative tab full of filled jars")
                    .define("jar_tab", false);
            CUSTOM_CONFIGURED_SCREEN = builder.comment("Enables custom Configured config screen")
                    .define("custom_configured_screen", true);

            DEBUG_RESOURCES = builder.comment("Save generated resources to disk in a 'debug' folder in your game directory. Mainly for debug purposes but can be used to generate assets in all wood types for your mods :0")
                    .define("debug_save_dynamic_pack", false);
            builder.pop();


            builder.push("blocks");
            PLANTER_ENABLED = builder.define(RegistryConstants.PLANTER_NAME, true);
            CLOCK_ENABLED = builder.define(RegistryConstants.CLOCK_BLOCK_NAME, true);
            PEDESTAL_ENABLED = builder.define(RegistryConstants.PEDESTAL_NAME, true);
            WIND_VANE_ENABLED = builder.define(RegistryConstants.WIND_VANE_NAME, true);
            ILLUMINATOR_ENABLED = builder.define(RegistryConstants.REDSTONE_ILLUMINATOR_NAME, true);
            NOTICE_BOARD_ENABLED = builder.define(RegistryConstants.NOTICE_BOARD_NAME, true);
            CRANK_ENABLED = builder.define(RegistryConstants.CRANK_NAME, true);
            JAR_ENABLED = builder.define(RegistryConstants.JAR_NAME, true);
            FAUCET_ENABLED = builder.define(RegistryConstants.FAUCET_NAME, true);
            TURN_TABLE_ENABLED = builder.define(RegistryConstants.TURN_TABLE_NAME, true);
            PISTON_LAUNCHER_ENABLED = builder.define(RegistryConstants.SPRING_LAUNCHER_NAME, true);
            SPEAKER_BLOCK_ENABLED = builder.define(RegistryConstants.SPEAKER_BLOCK_NAME, true);
            SIGN_POST_ENABLED = builder.define(RegistryConstants.SIGN_POST_NAME, true);
            HANGING_SIGN_ENABLED = builder.define(RegistryConstants.HANGING_SIGN_NAME, true);
            //WALL_LANTERN_ENABLED = builder.define("wall_lantern", true);
            BELLOWS_ENABLED = builder.define(RegistryConstants.BELLOWS_NAME, true);
            SCONCE_ENABLED = builder.define(RegistryConstants.SCONCE_NAME, true);
            SCONCE_GREEN_ENABLED = builder.define(RegistryConstants.SCONCE_NAME_GREEN, false);
            CAGE_ENABLED = builder.define(RegistryConstants.CAGE_NAME, true);
            ITEM_SHELF_ENABLED = builder.define(RegistryConstants.ITEM_SHELF_NAME, true);
            SCONCE_LEVER_ENABLED = builder.define(RegistryConstants.SCONCE_LEVER_NAME, true);
            COG_BLOCK_ENABLED = builder.define(RegistryConstants.COG_BLOCK_NAME, true);
            GLOBE_ENABLED = builder.define(RegistryConstants.GLOBE_NAME, true);
            HOURGLASS_ENABLED = builder.define(RegistryConstants.HOURGLASS_NAME, true);
            SACK_ENABLED = builder.define(RegistryConstants.SACK_NAME, true);
            BLACKBOARD_ENABLED = builder.define(RegistryConstants.BLACKBOARD_NAME, true);
            SAFE_ENABLED = builder.define(RegistryConstants.SAFE_NAME, true);
            COPPER_LANTERN_ENABLED = builder.define(RegistryConstants.COPPER_LANTERN_NAME, true);
            GOLD_TRAPDOOR_ENABLED = builder.define(RegistryConstants.GOLD_TRAPDOOR_NAME, true);
            GOLD_DOOR_ENABLED = builder.define(RegistryConstants.GOLD_DOOR_NAME, true);
            BAMBOO_SPIKES_ENABLED = builder.define(RegistryConstants.BAMBOO_SPIKES_NAME, true);
            TIPPED_SPIKES_ENABLED = builder.define(RegistryConstants.TIPPED_SPIKES_NAME, true);
            STONE_LAMP_ENABLED = builder.define(RegistryConstants.STONE_LAMP_NAME, true);
            END_STONE_LAMP_ENABLED = builder.define(RegistryConstants.END_STONE_LAMP_NAME, true);
            BLACKSTONE_LAMP_ENABLED = builder.define(RegistryConstants.BLACKSTONE_LAMP_NAME, true);
            DEEPSLATE_LAMP_ENABLED = builder.define(RegistryConstants.DEEPSLATE_LAMP_NAME, true);
            CHECKERBOARD_ENABLED = builder.define(RegistryConstants.CHECKER_BLOCK_NAME, true);
            NETHERITE_DOOR_ENABLED = builder.define(RegistryConstants.NETHERITE_DOOR_NAME, true);
            NETHERITE_TRAPDOOR_ENABLED = builder.define(RegistryConstants.NETHERITE_TRAPDOOR_NAME, true);
            LOCK_BLOCK_ENABLED = builder.define(RegistryConstants.LOCK_BLOCK_NAME, true);
            FLAX_ENABLED = builder.define(RegistryConstants.FLAX_NAME, true);
            ROPE_ENABLED = builder.define(RegistryConstants.ROPE_NAME, true);
            PULLEY_ENABLED = builder.define(RegistryConstants.PULLEY_BLOCK_NAME, true);
            FODDER_ENABLED = builder.define(RegistryConstants.FODDER_NAME, true);

            CRIMSON_LANTERN_ENABLED = builder.define(RegistryConstants.CRIMSON_LANTERN_NAME, true);
            MAGMA_CREAM_BLOCK_ENABLED = builder.define(RegistryConstants.MAGMA_CREAM_BLOCK_NAME, true);
            DAUB_ENABLED = builder.define(RegistryConstants.DAUB_NAME, true);
            WATTLE_AND_DAUB_ENABLED = builder.define("wattle_and_daub", true);
            TIMBER_FRAME_ENABLED = builder.define(RegistryConstants.TIMBER_FRAME_NAME, true);
            FLAG_ENABLED = builder.define(RegistryConstants.FLAG_NAME, true);
            TILE_ENABLED = builder.define(RegistryConstants.STONE_TILE_NAME, true);
            GOBLET_ENABLED = builder.define(RegistryConstants.GOBLET_NAME, true);
            RAKED_GRAVEL_ENABLED = builder.define(RegistryConstants.RAKED_GRAVEL_NAME, true);
            STATUE_ENABLED = builder.define(RegistryConstants.STATUE_NAME, true);
            IRON_GATE_ENABLED = builder.define(RegistryConstants.IRON_GATE_NAME, true);
            FEATHER_BLOCK_ENABLED = builder.define(RegistryConstants.FEATHER_BLOCK_NAME, true);
            FLINT_BLOCK_ENABLED = builder.define(RegistryConstants.FLINT_BLOCK_NAME, true);
            URN_ENABLED = builder.define(RegistryConstants.URN_NAME, true);
            ASH_ENABLED = builder.define(RegistryConstants.ASH_NAME, true);
            ASH_BRICKS_ENABLED = builder.define(RegistryConstants.ASH_BRICKS_NAME, true);
            DOORMAT_ENABLED = builder.define(RegistryConstants.DOORMAT_NAME, true);
            FLOWER_BOX_ENABLED = builder.define(RegistryConstants.FLOWER_BOX_NAME, true);
            BLACKSTONE_TILE_ENABLED = builder.define(RegistryConstants.BLACKSTONE_TILE_NAME, true);
            GLOBE_SEPIA = builder.define(RegistryConstants.GLOBE_SEPIA_NAME, true);
            PRESENT_ENABLED = builder.define(RegistryConstants.PRESENT_NAME, true);
            PRESENT_ENABLED = builder.define(RegistryConstants.STATUE_NAME, true);


            SILVER_TRAPDOOR_ENABLED = builder.define(RegistryConstants.SILVER_TRAPDOOR_NAME, true);
            SILVER_DOOR_ENABLED = builder.define(RegistryConstants.SILVER_DOOR_NAME, true);
            LEAD_TRAPDOOR_ENABLED = builder.define(RegistryConstants.LEAD_TRAPDOOR_NAME, true);
            LEAD_DOOR_ENABLED = builder.define(RegistryConstants.LEAD_DOOR_NAME, true);

            builder.pop();

            builder.push("items");
            FLUTE_ENABLED = builder.define(RegistryConstants.FLUTE_NAME, true);
            STASIS_ENABLED = builder.define(RegistryConstants.STASIS_NAME, true);
            DISPENSER_MINECART_ENABLED = builder.define(RegistryConstants.DISPENSER_MINECART_NAME, true);
            SOAP_ENABLED = builder.define(RegistryConstants.SOAP_NAME, true);
            BUBBLE_BLOWER_ENABLED = builder.define(RegistryConstants.BUBBLE_BLOWER_NAME, true);
            ANTIQUE_INK_ENABLED = builder.define(RegistryConstants.ANTIQUE_INK_NAME, true);
            SHULKER_HELMET_ENABLED = builder.define("shulker_shell", true);
            CANDY_ENABLED = builder.define(RegistryConstants.CANDY_NAME, true);
            WRENCH_ENABLED = builder.define(RegistryConstants.WRENCH_NAME, true);
            SLINGSHOT_ENABLED = builder.define(RegistryConstants.SLINGSHOT_NAME, true);
            ROPE_ARROW_ENABLED = builder.define(RegistryConstants.ROPE_ARROW_NAME, true);
            BOMB_ENABLED = builder.define(RegistryConstants.BOMB_NAME, true);
            PANCAKES_ENABLED = builder.define(RegistryConstants.PANCAKE_NAME, true);
            builder.pop();

            builder.push("entities");
            //FIREFLY_ENABLED = builder.define(RegistryConstants.FIREFLY_NAME, true);
            builder.pop();

            builder.comment("Here you can disable mixins if they clash with other mods ones")
                    .push("mixins");
            List<String> mixins = MixinConfigs.getMixinClassesNames();
            for (String c : mixins) {
                MIXIN_VALUES.put(c, builder.define(c.replace("Mixin", ""), true));
            }
            builder.pop();

        }


    }

    private static boolean hasMod(String... modIds) {
        return Arrays.stream(modIds).anyMatch(ModList.get()::isLoaded);
    }

}