package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.RegistryConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

//loaded before registry
public class RegistryConfigs {

    public static void init() {
    }

    public static final ConfigSpec REGISTRY_SPEC;


    private static final Map<String, Supplier<Boolean>> CONFIGS_BY_NAME = new HashMap<>();
    private static final Map<String, BooleanSupplier> MIXIN_VALUES = new HashMap<>();


    public static final Supplier<Boolean> ASH_ENABLED;
    public static final Supplier<Boolean> ASH_BRICKS_ENABLED;
    public static final Supplier<Boolean> PLANTER_ENABLED;
    public static final Supplier<Boolean> CLOCK_ENABLED;
    public static final Supplier<Boolean> PEDESTAL_ENABLED;
    public static final Supplier<Boolean> WIND_VANE_ENABLED;
    public static final Supplier<Boolean> ILLUMINATOR_ENABLED;
    public static final Supplier<Boolean> NOTICE_BOARD_ENABLED;
    public static final Supplier<Boolean> CRANK_ENABLED;
    public static final Supplier<Boolean> JAR_ENABLED;
    public static final Supplier<Boolean> FAUCET_ENABLED;
    public static final Supplier<Boolean> TURN_TABLE_ENABLED;
    public static final Supplier<Boolean> PISTON_LAUNCHER_ENABLED;
    public static final Supplier<Boolean> SPEAKER_BLOCK_ENABLED;
    public static final Supplier<Boolean> SIGN_POST_ENABLED;
    public static final Supplier<Boolean> HANGING_SIGN_ENABLED;
    public static final Supplier<Boolean> BELLOWS_ENABLED;
    public static final Supplier<Boolean> SCONCE_ENABLED;
    public static final Supplier<Boolean> SCONCE_GREEN_ENABLED;
    public static final Supplier<Boolean> CAGE_ENABLED;
    public static final Supplier<Boolean> ITEM_SHELF_ENABLED;
    public static final Supplier<Boolean> SCONCE_LEVER_ENABLED;
    public static final Supplier<Boolean> COG_BLOCK_ENABLED;
    public static final Supplier<Boolean> STONE_LAMP_ENABLED;
    public static final Supplier<Boolean> END_STONE_LAMP_ENABLED;
    public static final Supplier<Boolean> BLACKSTONE_LAMP_ENABLED;
    public static final Supplier<Boolean> DEEPSLATE_LAMP_ENABLED;
    public static final Supplier<Boolean> GLOBE_ENABLED;
    public static final Supplier<Boolean> HOURGLASS_ENABLED;
    public static final Supplier<Boolean> FLAG_ENABLED;
    public static final Supplier<Boolean> SACK_ENABLED;
    public static final Supplier<Boolean> BLACKBOARD_ENABLED;
    public static final Supplier<Boolean> SAFE_ENABLED;
    public static final Supplier<Boolean> COPPER_LANTERN_ENABLED;
    public static final Supplier<Boolean> FLUTE_ENABLED;
    public static final Supplier<Boolean> GOLD_TRAPDOOR_ENABLED;
    public static final Supplier<Boolean> GOLD_DOOR_ENABLED;
    public static final Supplier<Boolean> BAMBOO_SPIKES_ENABLED;
    public static final Supplier<Boolean> TIPPED_SPIKES_ENABLED;
    public static final Supplier<Boolean> CHECKERBOARD_ENABLED;
    public static final Supplier<Boolean> NETHERITE_TRAPDOOR_ENABLED;
    public static final Supplier<Boolean> NETHERITE_DOOR_ENABLED;
    public static final Supplier<Boolean> PANCAKES_ENABLED;
    public static final Supplier<Boolean> LOCK_BLOCK_ENABLED;
    public static final Supplier<Boolean> FLAX_ENABLED;
    public static final Supplier<Boolean> ROPE_ENABLED;
    public static final Supplier<Boolean> ROPE_ARROW_ENABLED;
    public static final Supplier<Boolean> PULLEY_ENABLED;
    public static final Supplier<Boolean> FODDER_ENABLED;
    public static final Supplier<Boolean> BOMB_ENABLED;
    public static final Supplier<Boolean> CRIMSON_LANTERN_ENABLED;
    public static final Supplier<Boolean> DAUB_ENABLED;
    public static final Supplier<Boolean> WATTLE_AND_DAUB_ENABLED;
    public static final Supplier<Boolean> TIMBER_FRAME_ENABLED;
    public static final Supplier<Boolean> TILE_ENABLED;
    public static final Supplier<Boolean> GOBLET_ENABLED;
    public static final Supplier<Boolean> RAKED_GRAVEL_ENABLED;
    public static final Supplier<Boolean> STATUE_ENABLED;
    public static final Supplier<Boolean> IRON_GATE_ENABLED;
    public static final Supplier<Boolean> FEATHER_BLOCK_ENABLED;
    public static final Supplier<Boolean> FLINT_BLOCK_ENABLED;
    public static final Supplier<Boolean> SLINGSHOT_ENABLED;
    public static final Supplier<Boolean> SHULKER_HELMET_ENABLED;
    public static final Supplier<Boolean> CANDY_ENABLED;
    public static final Supplier<Boolean> WRENCH_ENABLED;
    public static final Supplier<Boolean> QUIVER_ENABLED;
    public static final Supplier<Boolean> URN_ENABLED;
    public static final Supplier<Boolean> ANTIQUE_INK_ENABLED;
    public static final Supplier<Boolean> DOORMAT_ENABLED;
    public static final Supplier<Boolean> FLOWER_BOX_ENABLED;
    public static final Supplier<Boolean> BLACKSTONE_TILE_ENABLED;
    public static final Supplier<Boolean> SOAP_ENABLED;
    public static final Supplier<Boolean> BUBBLE_BLOWER_ENABLED;
    public static final Supplier<Boolean> GLOBE_SEPIA;
    public static final Supplier<Boolean> PRESENT_ENABLED;
    public static final Supplier<Boolean> STASIS_ENABLED;
    public static final Supplier<Boolean> SILVER_TRAPDOOR_ENABLED;
    public static final Supplier<Boolean> SILVER_DOOR_ENABLED;
    public static final Supplier<Boolean> LEAD_TRAPDOOR_ENABLED;
    public static final Supplier<Boolean> LEAD_DOOR_ENABLED;
    public static final Supplier<Boolean> DISPENSER_MINECART_ENABLED;
    public static final Supplier<Boolean> SUGAR_CUBE_ENABLED;
    public static final Supplier<Boolean> CRYSTAL_DISPLAY_ENABLED;
    public static final Supplier<Boolean> LAPIS_BRICKS_ENABLED;
    public static final Supplier<Boolean> RELAYER_ENABLED;
    public static final Supplier<Boolean> CANDLE_HOLDER_ENABLED;
    public static final Supplier<Boolean> ENDERMAN_HEAD_ENABLED;

    public static final Supplier<Boolean> JAR_TAB;
    public static final Supplier<Boolean> CREATIVE_TAB;
    public static final Supplier<Boolean> DISPENSERS;
    public static final Supplier<Boolean> CUSTOM_CONFIGURED_SCREEN;
    public static final Supplier<Boolean> DEBUG_RESOURCES;
    public static final Supplier<Boolean> PACK_DEPENDANT_ASSETS;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Supplementaries.res("registry"), ConfigType.COMMON);


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

        PACK_DEPENDANT_ASSETS = () -> true;
        //PACK_DEPENDANT_ASSETS = builder.comment("Allows generated assets to depend on installed resource and data packs. " +
        //        "This means that if for example you have a texture pack that changes the planks texture all generated signs textures will be based off that one insted" +
        //       "Disable to have it only use vanilla assets").define("pack_dependant_assets", true);
        builder.pop();


        builder.push("blocks");
        PLANTER_ENABLED = regConfig(builder, RegistryConstants.PLANTER_NAME, true);
        CLOCK_ENABLED = regConfig(builder, RegistryConstants.CLOCK_BLOCK_NAME, true);
        PEDESTAL_ENABLED = regConfig(builder, RegistryConstants.PEDESTAL_NAME, true);
        WIND_VANE_ENABLED = regConfig(builder, RegistryConstants.WIND_VANE_NAME, true);
        ILLUMINATOR_ENABLED = regConfig(builder, RegistryConstants.REDSTONE_ILLUMINATOR_NAME, true);
        NOTICE_BOARD_ENABLED = regConfig(builder, RegistryConstants.NOTICE_BOARD_NAME, true);
        CRANK_ENABLED = regConfig(builder, RegistryConstants.CRANK_NAME, true);
        JAR_ENABLED = regConfig(builder, RegistryConstants.JAR_NAME, true);
        FAUCET_ENABLED = regConfig(builder, RegistryConstants.FAUCET_NAME, true);
        TURN_TABLE_ENABLED = regConfig(builder, RegistryConstants.TURN_TABLE_NAME, true);
        PISTON_LAUNCHER_ENABLED = regConfig(builder, RegistryConstants.SPRING_LAUNCHER_NAME, true);
        SPEAKER_BLOCK_ENABLED = regConfig(builder, RegistryConstants.SPEAKER_BLOCK_NAME, true);
        SIGN_POST_ENABLED = regConfig(builder, RegistryConstants.SIGN_POST_NAME, true);
        HANGING_SIGN_ENABLED = regConfig(builder, RegistryConstants.HANGING_SIGN_NAME, true);
        BELLOWS_ENABLED = regConfig(builder, RegistryConstants.BELLOWS_NAME, true);
        SCONCE_ENABLED = regConfig(builder, RegistryConstants.SCONCE_NAME, true);
        SCONCE_GREEN_ENABLED = regConfig(builder, RegistryConstants.SCONCE_NAME_GREEN, false);
        CAGE_ENABLED = regConfig(builder, RegistryConstants.CAGE_NAME, true);
        ITEM_SHELF_ENABLED = regConfig(builder, RegistryConstants.ITEM_SHELF_NAME, true);
        SCONCE_LEVER_ENABLED = regConfig(builder, RegistryConstants.SCONCE_LEVER_NAME, true);
        COG_BLOCK_ENABLED = regConfig(builder, RegistryConstants.COG_BLOCK_NAME, true);
        GLOBE_ENABLED = regConfig(builder, RegistryConstants.GLOBE_NAME, true);
        HOURGLASS_ENABLED = regConfig(builder, RegistryConstants.HOURGLASS_NAME, true);
        SACK_ENABLED = regConfig(builder, RegistryConstants.SACK_NAME, true);
        BLACKBOARD_ENABLED = regConfig(builder, RegistryConstants.BLACKBOARD_NAME, true);
        SAFE_ENABLED = regConfig(builder, RegistryConstants.SAFE_NAME, true);
        COPPER_LANTERN_ENABLED = regConfig(builder, RegistryConstants.COPPER_LANTERN_NAME, true);
        GOLD_TRAPDOOR_ENABLED = regConfig(builder, RegistryConstants.GOLD_TRAPDOOR_NAME, true);
        GOLD_DOOR_ENABLED = regConfig(builder, RegistryConstants.GOLD_DOOR_NAME, true);
        BAMBOO_SPIKES_ENABLED = regConfig(builder, RegistryConstants.BAMBOO_SPIKES_NAME, true);
        TIPPED_SPIKES_ENABLED = regConfig(builder, RegistryConstants.TIPPED_SPIKES_NAME, true);
        STONE_LAMP_ENABLED = regConfig(builder, RegistryConstants.STONE_LAMP_NAME, true);
        END_STONE_LAMP_ENABLED = regConfig(builder, RegistryConstants.END_STONE_LAMP_NAME, true);
        BLACKSTONE_LAMP_ENABLED = regConfig(builder, RegistryConstants.BLACKSTONE_LAMP_NAME, true);
        DEEPSLATE_LAMP_ENABLED = regConfig(builder, RegistryConstants.DEEPSLATE_LAMP_NAME, true);
        CHECKERBOARD_ENABLED = regConfig(builder, RegistryConstants.CHECKER_BLOCK_NAME, true);
        NETHERITE_DOOR_ENABLED = regConfig(builder, RegistryConstants.NETHERITE_DOOR_NAME, true);
        NETHERITE_TRAPDOOR_ENABLED = regConfig(builder, RegistryConstants.NETHERITE_TRAPDOOR_NAME, true);
        LOCK_BLOCK_ENABLED = regConfig(builder, RegistryConstants.LOCK_BLOCK_NAME, true);
        FLAX_ENABLED = regConfig(builder, RegistryConstants.FLAX_NAME, true);
        ROPE_ENABLED = regConfig(builder.comment("""
                Before disabling because other mods dont have such easy configs let me remind you that my ropes can:
                - be pulled up and down
                - be placed horizontally and walked upon
                - be tied to fences, walls and posts, horizontals too
                - be found in new mineshaft structures and chests
                - have custom break slide and step sound
                - negate all fall damage while not slowing your fall
                - rope arrows and pulleys
                - you can ring attached bells
                - mod integration (FD tomatoes)
                - can pull the last block attached to them
                - walking on one will wobble your screen"""), RegistryConstants.ROPE_NAME, true);
        PULLEY_ENABLED = regConfig(builder, RegistryConstants.PULLEY_BLOCK_NAME, true);
        FODDER_ENABLED = regConfig(builder, RegistryConstants.FODDER_NAME, true);

        CRIMSON_LANTERN_ENABLED = regConfig(builder, RegistryConstants.CRIMSON_LANTERN_NAME, true);
        DAUB_ENABLED = regConfig(builder, RegistryConstants.DAUB_NAME, true);
        WATTLE_AND_DAUB_ENABLED = regConfig(builder, "wattle_and_daub", true);
        TIMBER_FRAME_ENABLED = regConfig(builder, RegistryConstants.TIMBER_FRAME_NAME, true);
        FLAG_ENABLED = regConfig(builder, RegistryConstants.FLAG_NAME, true);
        TILE_ENABLED = regConfig(builder, RegistryConstants.STONE_TILE_NAME, true);
        GOBLET_ENABLED = regConfig(builder, RegistryConstants.GOBLET_NAME, true);
        RAKED_GRAVEL_ENABLED = regConfig(builder, RegistryConstants.RAKED_GRAVEL_NAME, true);
        STATUE_ENABLED = regConfig(builder, RegistryConstants.STATUE_NAME, true);
        IRON_GATE_ENABLED = regConfig(builder, RegistryConstants.IRON_GATE_NAME, true);
        FEATHER_BLOCK_ENABLED = regConfig(builder, RegistryConstants.FEATHER_BLOCK_NAME, true);
        FLINT_BLOCK_ENABLED = regConfig(builder, RegistryConstants.FLINT_BLOCK_NAME, true);
        URN_ENABLED = regConfig(builder, RegistryConstants.URN_NAME, true);
        ASH_ENABLED = regConfig(builder, RegistryConstants.ASH_NAME, true);
        ASH_BRICKS_ENABLED = regConfig(builder, RegistryConstants.ASH_BRICKS_NAME, true);
        DOORMAT_ENABLED = regConfig(builder, RegistryConstants.DOORMAT_NAME, true);
        FLOWER_BOX_ENABLED = regConfig(builder, RegistryConstants.FLOWER_BOX_NAME, true);
        BLACKSTONE_TILE_ENABLED = regConfig(builder, RegistryConstants.BLACKSTONE_TILE_NAME, true);
        GLOBE_SEPIA = regConfig(builder, RegistryConstants.GLOBE_SEPIA_NAME, true);
        PRESENT_ENABLED = regConfig(builder, RegistryConstants.PRESENT_NAME, true);
        SUGAR_CUBE_ENABLED = regConfig(builder, RegistryConstants.SUGAR_CUBE_NAME, true);
        CRYSTAL_DISPLAY_ENABLED = regConfig(builder, RegistryConstants.CRYSTAL_DISPLAY_NAME, true);
        LAPIS_BRICKS_ENABLED = regConfig(builder, RegistryConstants.LAPIS_BRICKS_NAME, true);
        RELAYER_ENABLED = regConfig(builder, RegistryConstants.RELAYER_NAME, true);
        CANDLE_HOLDER_ENABLED = regConfig(builder, RegistryConstants.CANDLE_HOLDER_NAME, true);
        ENDERMAN_HEAD_ENABLED = regConfig(builder, RegistryConstants.ENDERMAN_HEAD_NAME, true);

        SILVER_TRAPDOOR_ENABLED = regConfig(builder, RegistryConstants.SILVER_TRAPDOOR_NAME, true);
        SILVER_DOOR_ENABLED = regConfig(builder, RegistryConstants.SILVER_DOOR_NAME, true);
        LEAD_TRAPDOOR_ENABLED = regConfig(builder, RegistryConstants.LEAD_TRAPDOOR_NAME, true);
        LEAD_DOOR_ENABLED = regConfig(builder, RegistryConstants.LEAD_DOOR_NAME, true);

        builder.pop();

        builder.push("items");
        FLUTE_ENABLED = regConfig(builder, RegistryConstants.FLUTE_NAME, true);
        STASIS_ENABLED = regConfig(builder, RegistryConstants.STASIS_NAME, true);
        DISPENSER_MINECART_ENABLED = regConfig(builder, RegistryConstants.DISPENSER_MINECART_NAME, true);
        SOAP_ENABLED = regConfig(builder, RegistryConstants.SOAP_NAME, true);
        BUBBLE_BLOWER_ENABLED = regConfig(builder, RegistryConstants.BUBBLE_BLOWER_NAME, true);
        ANTIQUE_INK_ENABLED = regConfig(builder, RegistryConstants.ANTIQUE_INK_NAME, true);
        SHULKER_HELMET_ENABLED = regConfig(builder, "shulker_shell", true);
        CANDY_ENABLED = regConfig(builder, RegistryConstants.CANDY_NAME, true);
        WRENCH_ENABLED = regConfig(builder, RegistryConstants.WRENCH_NAME, true);
        QUIVER_ENABLED = regConfig(builder, RegistryConstants.QUIVER_NAME, true);
        SLINGSHOT_ENABLED = regConfig(builder, RegistryConstants.SLINGSHOT_NAME, true);
        ROPE_ARROW_ENABLED = regConfig(builder, RegistryConstants.ROPE_ARROW_NAME, true);
        BOMB_ENABLED = regConfig(builder, RegistryConstants.BOMB_NAME, true);
        PANCAKES_ENABLED = regConfig(builder, RegistryConstants.PANCAKE_NAME, true);
        builder.pop();

        /*
        builder.comment("Here you can disable mixins if they clash with other mods ones")
                .push("mixins");
        List<String> mixins = MixinConfigs.getMixinClassesNames();
        for (String c : mixins) {
            MIXIN_VALUES.put(c, regConfig(builder, c.replace("Mixin", ""), true));
        }
        builder.pop();
        */


        REGISTRY_SPEC = builder.build();
        //load early
        REGISTRY_SPEC.loadFromFile();

        warnIfTooManyOff();
        builder.onChange(RegistryConfigs::warnIfTooManyOff);
    }

    private static void warnIfTooManyOff() {
        int size = CONFIGS_BY_NAME.size();
        int on = 0;
        for (var v : CONFIGS_BY_NAME.values()) {
            if (v.get()) on++;
        }
        if (on / (float) size < 0.2) {
            Supplementaries.LOGGER.warn("You seem to have disabled more than 80% of the mod. You should probably remove it");
        }
    }

    public static boolean isMixinEnabled(String className) {
        BooleanSupplier config = RegistryConfigs.MIXIN_VALUES.get(className);
        return config == null || config.getAsBoolean();
    }

    private static Supplier<Boolean> regConfig(ConfigBuilder builder, String name, Boolean value) {
        var config = builder.define(name, value);
        CONFIGS_BY_NAME.put(name, config);
        return config;
    }

    public static boolean isEnabled(String key) {
        if (key.contains("daub")) return DAUB_ENABLED.get();
        return switch (key) {
            case "way_sign" -> CommonConfigs.Spawns.WAY_SIGN_ENABLED.get();
            case RegistryConstants.TRAPPED_PRESENT_NAME -> PRESENT_ENABLED.get();
            case RegistryConstants.FLAX_BLOCK_NAME, RegistryConstants.FLAX_WILD_NAME ->
                    RegistryConfigs.FLAX_ENABLED.get();
            case RegistryConstants.SOAP_BLOCK_NAME -> RegistryConfigs.SOAP_ENABLED.get();
            case RegistryConstants.CHECKER_SLAB_NAME, RegistryConstants.CHECKER_VERTICAL_SLAB_NAME ->
                    CHECKERBOARD_ENABLED.get();
            case "planter_rich", "planter_rich_soul" -> PLANTER_ENABLED.get();
            case "vertical_slabs" -> CompatHandler.isVerticalSlabEnabled();
            case RegistryConstants.GLOBE_SEPIA_NAME -> GLOBE_SEPIA.get() && ANTIQUE_INK_ENABLED.get();
            case RegistryConstants.KEY_NAME ->
                    NETHERITE_DOOR_ENABLED.get() || NETHERITE_TRAPDOOR_ENABLED.get() || SAFE_ENABLED.get();
            default -> CONFIGS_BY_NAME.getOrDefault(key, () -> true).get();
        };
    }

}