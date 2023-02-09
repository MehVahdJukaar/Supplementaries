package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TestConfigs {

    public static void init() {
    }

    private static final Map<String, Supplier<Boolean>> FEATURE_TOGGLES = new HashMap<>();

    public static final ConfigSpec SPEC;

    private static final WeakReference<ConfigBuilder> builderReference;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Supplementaries.res("test"), ConfigType.COMMON);

        builderReference = new WeakReference<>(builder);

        TestConfigs.Redstone.init();
        TestConfigs.Utilities.init();
        TestConfigs.Building.init();
        TestConfigs.Tools.init();

        builder.setSynced();
      //  builder.onChange(TestConfigs::onRefresh);

        SPEC = builder.buildAndRegister();
        SPEC.loadFromFile();
    }


/*
        private static Supplier<Holder.Reference<Block>> ropeOverride = () -> null;



        public static final Supplier<Integer> BUBBLE_LIFETIME;
        public static final Supplier<Boolean> BUBBLE_BREAK;
        public static final Supplier<Boolean> BUBBLE_FEATHER_FALLING;









    public static final Supplier<Boolean> DISTANCE_TEXT;
    public static final Supplier<Boolean> WAY_SIGN_ENABLED;

    public static final Supplier<Boolean> WILD_FLAX_ENABLED;
    public static final Supplier<Integer> FLAX_PATCH_TRIES;
    public static final Supplier<Integer> FLAX_AVERAGE_EVERY;

    public static final Supplier<Boolean> URN_PILE_ENABLED;
    public static final Supplier<Integer> URN_PATCH_TRIES;
    public static final Supplier<Integer> URN_PER_CHUNK;


    public static final Supplier<Double> RED_MERCHANT_SPAWN_MULTIPLIER;


    public static final Supplier<Integer> ENDERMAN_HEAD_INCREMENT;
    public static final Supplier<Boolean> ENDERMAN_HEAD_WORKS_FROM_ANY_SIDE;
    */

    public static class Redstone {

        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("redstone");

            builder.push("speaker_block");
            SPEAKER_BLOCK_ENABLED = feature(builder);
            SPEAKER_NARRATOR = builder.comment("Enable/disable speaker block narrator mode")
                    .define("narrator_enabled", true);
            SPEAKER_RANGE = builder.comment("Maximum block range")
                    .define("range", 64, 0, 100000000);
            builder.pop();

            builder.push("bellows");
            BELLOWS_ENABLED = feature(builder);
            BELLOWS_PERIOD = builder.comment("""
                            bellows pushes air following this equation:\s
                            air=(sin(2PI*ticks/period)<0), with period = base_period-(redstone_power-1)*power_scaling\s
                            represents base period at 1 power""")
                    .define("base_period", 78, 1, 512);
            BELLOWS_POWER_SCALING = builder.comment("how much the period changes in relation to the block redstone power")
                    .define("power_scaling", 3, 0, 128);
            BELLOWS_BASE_VEL_SCALING = builder.comment("""
                            velocity increase uses this equation:\s
                            vel = base_vel*((range-entity_distance)/range) with base_vel = base_velocity_scaling/period\s
                            note that the block will push further the faster it's pulsing""")
                    .define("base_velocity_scaling", 5.0, 0.0, 64);
            BELLOWS_MAX_VEL = builder.comment("entities with velocity greater than this won't be pushed")
                    .define("power_scaling", 2.0, 0.0, 16);
            BELLOWS_FLAG = builder.comment("sets velocity changed flag when pushing entities +\n" +
                            "causes pushing animation to be smooth client side but also restricts player movement when being pushed")
                    .define("velocity_changed_flag", true);
            BELLOWS_RANGE = builder.comment("maximum range")
                    .comment("note that it will still only keep alive the two fire blocks closer to it")
                    .define("range", 5, 0, 16);
            builder.pop();

            builder.push("spring_launcher");
            PISTON_LAUNCHER_ENABLED = feature(builder);
            LAUNCHER_VEL = builder.comment("spring launcher launch speed")
                    .define("velocity", 1.5D, 0, 16);
            LAUNCHER_HEIGHT = builder.comment("fall distance needed to trigger the automatic spring launch")
                    .define("fall_height_required", 5, 0, 512);
            builder.pop();

            builder.push("turn_table");
            TURN_TABLE_ENABLED = feature(builder);
            TURN_TABLE_ROTATE_ENTITIES = builder.comment("can rotate entities standing on it?")
                    .define("rotate_entities", true);
            builder.pop();

            WIND_VANE_ENABLED = feature(builder, ModConstants.WIND_VANE_NAME);
            CLOCK_ENABLED = feature(builder, ModConstants.CLOCK_BLOCK_NAME);
            ILLUMINATOR_ENABLED = feature(builder, ModConstants.REDSTONE_ILLUMINATOR_NAME);
            CRANK_ENABLED = feature(builder, ModConstants.CRANK_NAME);
            FAUCET_ENABLED = feature(builder, ModConstants.FAUCET_NAME);
            COG_BLOCK_ENABLED = feature(builder, ModConstants.COG_BLOCK_NAME);
            GOLD_DOOR_ENABLED = feature(builder, ModConstants.GOLD_DOOR_NAME);
            GOLD_TRAPDOOR_ENABLED = feature(builder, ModConstants.GOLD_TRAPDOOR_NAME);
            LOCK_BLOCK_ENABLED = feature(builder, ModConstants.LOCK_BLOCK_NAME);
            DISPENSER_MINECART_ENABLED = feature(builder, ModConstants.DISPENSER_MINECART_NAME);
            CRYSTAL_DISPLAY_ENABLED = feature(builder, ModConstants.CRYSTAL_DISPLAY_NAME);
            RELAYER_ENABLED = feature(builder, ModConstants.RELAYER_NAME);
            PULLEY_ENABLED = feature(builder, ModConstants.PULLEY_BLOCK_NAME);

            builder.pop();
        }

        public static final Supplier<Boolean> SPEAKER_BLOCK_ENABLED;
        public static final Supplier<Integer> SPEAKER_RANGE;
        public static final Supplier<Boolean> SPEAKER_NARRATOR;

        public static final Supplier<Boolean> BELLOWS_ENABLED;
        public static final Supplier<Integer> BELLOWS_PERIOD;
        public static final Supplier<Integer> BELLOWS_POWER_SCALING;
        public static final Supplier<Double> BELLOWS_MAX_VEL;
        public static final Supplier<Double> BELLOWS_BASE_VEL_SCALING;
        public static final Supplier<Boolean> BELLOWS_FLAG;
        public static final Supplier<Integer> BELLOWS_RANGE;

        public static final Supplier<Boolean> PISTON_LAUNCHER_ENABLED;
        public static final Supplier<Double> LAUNCHER_VEL;
        public static final Supplier<Integer> LAUNCHER_HEIGHT;

        public static final Supplier<Boolean> TURN_TABLE_ENABLED;
        public static final Supplier<Boolean> TURN_TABLE_ROTATE_ENTITIES;

        public static final Supplier<Boolean> WIND_VANE_ENABLED;

        public static final Supplier<Boolean> CLOCK_ENABLED;

        public static final Supplier<Boolean> ILLUMINATOR_ENABLED;

        public static final Supplier<Boolean> CRANK_ENABLED;

        public static final Supplier<Boolean> FAUCET_ENABLED;

        public static final Supplier<Boolean> COG_BLOCK_ENABLED;

        public static final Supplier<Boolean> GOLD_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> GOLD_DOOR_ENABLED;

        public static final Supplier<Boolean> LOCK_BLOCK_ENABLED;

        public static final Supplier<Boolean> DISPENSER_MINECART_ENABLED;

        public static final Supplier<Boolean> RELAYER_ENABLED;

        public static final Supplier<Boolean> CRYSTAL_DISPLAY_ENABLED;

        public static final Supplier<Boolean> PULLEY_ENABLED;

    }


    public static class Building {
        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("building");

            builder.push("blackboard");
            BLACKBOARD_ENABLED = feature(builder);
            BLACKBOARD_COLOR = builder.comment("Enable to draw directly on a blackboard using any dye. Gui still only works in black and white")
                    .define("colored_blackboard", PlatformHelper.isModLoaded("chalk"));
            BLACKBOARD_MODE = builder.comment("Interaction mode for blackboards")
                    .define("interaction_mode", BlackboardBlock.UseMode.BOTH);
            builder.pop();

            builder.push("timber_frame");
            TIMBER_FRAME_ENABLED = feature(builder);
            SWAP_TIMBER_FRAME = builder.comment("Allow placing a timber frame directly on a block by holding shift")
                    .define("swap_on_shift", false);
            AXE_TIMBER_FRAME_STRIP = builder.comment("Allows axes to remove a framed block leaving the contained block intact")
                    .define("axes_strip", true);
            REPLACE_DAUB = builder.comment("Replace a timber frame with wattle and daub block when daub is placed in it")
                    .define("replace_daub", true);
            builder.pop();

            builder.push("iron_gate");
            IRON_GATE_ENABLED = feature(builder);
            DOUBLE_IRON_GATE = builder.comment("Allows two iron gates to be opened simultaneously when on top of the other")
                    .define("double_opening", true);
            CONSISTENT_GATE = builder.comment("Makes iron (ang gold) gates behave like their door counterpart so for example iron gates will only be openeable by redstone")
                    .define("door-like_gates", false);
            builder.pop();

            builder.push("item_shelf");
            ITEM_SHELF_ENABLED = feature(builder);
            ITEM_SHELF_LADDER = builder.comment("Makes item shelves climbable")
                    .define("climbable_shelves", false);
            builder.pop();

            builder.push("sugar_cube");
            SUGAR_CUBE_ENABLED = feature(builder);
            SUGAR_BLOCK_HORSE_SPEED_DURATION = builder.comment("Duration in seconts of speed effect garanted to horses that eat a sugar cube")
                    .define("horse_speed_duration", 10, 0, 1000);
            builder.pop();

            builder.push("planter");
            PLANTER_ENABLED = feature(builder);
            PLANTER_BREAKS = builder.comment("Makes so saplings that grow in a planter will break it turning into rooted dirt")
                    .define("broken_by_sapling", true);
            builder.pop();

            builder.push("notice_board");
            NOTICE_BOARD_ENABLED = feature(builder);
            NOTICE_BOARDS_UNRESTRICTED = builder.comment("Allows notice boards to accept and display any item, not just maps and books")
                    .define("allow_any_item", false);
            builder.pop();

            builder.push("pedestal");
            PEDESTAL_ENABLED = feature(builder);
            CRYSTAL_ENCHANTING = builder.comment("If enabled end crystals placed on a pedestals will provide an enchantment power bonus equivalent to 3 bookshelves")
                    .define("crystal_enchanting", 3, 0, 100);
            builder.pop();

            builder.push("ash");
            ASH_ENABLED = feature(builder);
            ASH_BURN = builder.comment("Burnable blocks will have a chance to create ash layers when burned")
                    .define("ash_from_fire", true);
            ASH_RAIN = builder.comment("Allows rain to wash away ash layers overtime")
                    .define("rain_wash_ash", true);
            builder.push("basalt_ash");
            BASALT_ASH_ENABLED = builder.define("enabled", true);
            BASALT_ASH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 36, 1, 1000);
            BASALT_ASH_PER_CHUNK = builder.comment("Spawn attempts per chunk. Increases spawn frequency")
                    .define("spawn_attempts", 15, 0, 100);
            builder.pop();
            builder.pop();

            builder.push("flag");
            FLAG_ENABLED = feature(builder);
            FLAG_POLE = builder.comment("Allows right/left clicking on a stick to lower/raise a flag attached to it")
                    .define("stick_pole", true);
            FLAG_POLE_LENGTH = builder.comment("Maximum allowed pole length")
                    .define("pole_length", 16, 0, 256);
            builder.pop();

            builder.push("goblet");
            GOBLET_ENABLED = feature(builder);
            GOBLET_DRINK = builder.comment("Allows drinking from goblets").define("allow_drinking", true);
            builder.pop();

            builder.push("globe");
            GLOBE_ENABLED = feature(builder);
            GLOBE_SEPIA = feature(builder, "sepia_globe");
            GLOBE_TRADES = builder.comment("How many globe trades to give to the wandering trader. This will effectively increase the chance of him having a globe trader. Increase this if you have other mods that add stuff to that trader")
                    .define("chance", 2, 0, 50);
            builder.pop();

            DAUB_ENABLED = feature(builder, ModConstants.DAUB_NAME);
            ASH_BRICKS_ENABLED = feature(builder, ModConstants.ASH_BRICK_NAME);
            LAPIS_BRICKS_ENABLED = feature(builder, ModConstants.LAPIS_BRICKS_NAME);
            DEEPSLATE_LAMP_ENABLED = feature(builder, ModConstants.DEEPSLATE_LAMP_NAME);
            END_STONE_LAMP_ENABLED = feature(builder, ModConstants.END_STONE_LAMP_NAME);
            BLACKSTONE_LAMP_ENABLED = feature(builder, ModConstants.BLACKSTONE_LAMP_NAME);
            STONE_LAMP_ENABLED = feature(builder, ModConstants.STONE_LAMP_NAME);
            TILE_ENABLED = feature(builder, ModConstants.STONE_TILE_NAME);
            BLACKSTONE_TILE_ENABLED = feature(builder, ModConstants.BLACKSTONE_TILE_NAME);
            SCONCE_ENABLED = feature(builder, ModConstants.SCONCE_NAME);
            SCONCE_LEVER_ENABLED = feature(builder, ModConstants.SCONCE_LEVER_NAME);
            SCONCE_GREEN_ENABLED = feature(builder, ModConstants.SCONCE_NAME_GREEN,false);
            PANCAKES_ENABLED = feature(builder, ModConstants.PANCAKE_NAME);
            NETHERITE_DOOR_ENABLED = feature(builder, ModConstants.NETHERITE_DOOR_NAME);
            NETHERITE_TRAPDOOR_ENABLED = feature(builder, ModConstants.NETHERITE_TRAPDOOR_NAME);
            SILVER_DOOR_ENABLED = feature(builder, ModConstants.SILVER_DOOR_NAME);
            SILVER_TRAPDOOR_ENABLED = feature(builder, ModConstants.SILVER_TRAPDOOR_NAME);
            LEAD_DOOR_ENABLED = feature(builder, ModConstants.LEAD_DOOR_NAME);
            LEAD_TRAPDOOR_ENABLED = feature(builder, ModConstants.LEAD_TRAPDOOR_NAME);
            SIGN_POST_ENABLED = feature(builder, ModConstants.SIGN_POST_NAME);
            HANGING_SIGN_ENABLED = feature(builder, ModConstants.HANGING_SIGN_NAME);
            CRIMSON_LANTERN_ENABLED = feature(builder, ModConstants.CRIMSON_LANTERN_NAME);
            COPPER_LANTERN_ENABLED = feature(builder, ModConstants.COPPER_LANTERN_NAME);
            CHECKERBOARD_ENABLED = feature(builder, ModConstants.CHECKER_BLOCK_NAME);
            RAKED_GRAVEL_ENABLED = feature(builder, ModConstants.RAKED_GRAVEL_NAME);
            FEATHER_BLOCK_ENABLED = feature(builder, ModConstants.FEATHER_BLOCK_NAME);
            STATUE_ENABLED = feature(builder, ModConstants.STATUE_NAME);
            FLOWER_BOX_ENABLED = feature(builder, ModConstants.FLOWER_BOX_NAME);
            DOORMAT_ENABLED = feature(builder, ModConstants.DOORMAT_NAME);
            FLINT_BLOCK_ENABLED = feature(builder, ModConstants.FLINT_BLOCK_NAME);
            CANDLE_HOLDER_ENABLED = feature(builder, ModConstants.CANDLE_HOLDER_NAME);


            builder.pop();
        }

        public static final Supplier<Boolean> BLACKBOARD_ENABLED;
        public static final Supplier<Boolean> BLACKBOARD_COLOR;
        public static final Supplier<BlackboardBlock.UseMode> BLACKBOARD_MODE;

        public static final Supplier<Boolean> IRON_GATE_ENABLED;
        public static final Supplier<Boolean> DOUBLE_IRON_GATE;
        public static final Supplier<Boolean> CONSISTENT_GATE;

        public static final Supplier<Boolean> ASH_BURN;
        public static final Supplier<Boolean> ASH_RAIN;
        public static final Supplier<Boolean> BASALT_ASH_ENABLED;
        public static final Supplier<Integer> BASALT_ASH_TRIES;
        public static final Supplier<Integer> BASALT_ASH_PER_CHUNK;

        public static final Supplier<Boolean> SUGAR_CUBE_ENABLED;
        public static final Supplier<Integer> SUGAR_BLOCK_HORSE_SPEED_DURATION;

        public static final Supplier<Boolean> ITEM_SHELF_ENABLED;
        public static final Supplier<Boolean> ITEM_SHELF_LADDER;

        public static final Supplier<Boolean> NOTICE_BOARD_ENABLED;
        public static final Supplier<Boolean> NOTICE_BOARDS_UNRESTRICTED;

        public static final Supplier<Boolean> FLAG_ENABLED;
        public static final Supplier<Boolean> FLAG_POLE;
        public static final Supplier<Integer> FLAG_POLE_LENGTH;

        public static final Supplier<Boolean> GOBLET_ENABLED;
        public static final Supplier<Boolean> GOBLET_DRINK;

        public static final Supplier<Boolean> PLANTER_ENABLED;
        public static final Supplier<Boolean> PLANTER_BREAKS;

        public static final Supplier<Boolean> GLOBE_ENABLED;
        public static final Supplier<Boolean> GLOBE_SEPIA;
        public static final Supplier<Integer> GLOBE_TRADES;

        public static final Supplier<Boolean> PEDESTAL_ENABLED;
        public static final Supplier<Integer> CRYSTAL_ENCHANTING;

        public static final Supplier<Boolean> TIMBER_FRAME_ENABLED;
        public static final Supplier<Boolean> REPLACE_DAUB;
        public static final Supplier<Boolean> SWAP_TIMBER_FRAME;
        public static final Supplier<Boolean> AXE_TIMBER_FRAME_STRIP;

        public static final Supplier<Boolean> DAUB_ENABLED;

        public static final Supplier<Boolean> ASH_ENABLED;

        public static final Supplier<Boolean> ASH_BRICKS_ENABLED;

        public static final Supplier<Boolean> SIGN_POST_ENABLED;

        public static final Supplier<Boolean> HANGING_SIGN_ENABLED;

        public static final Supplier<Boolean> SCONCE_ENABLED;

        public static final Supplier<Boolean> SCONCE_GREEN_ENABLED;

        public static final Supplier<Boolean> SCONCE_LEVER_ENABLED;

        public static final Supplier<Boolean> STONE_LAMP_ENABLED;

        public static final Supplier<Boolean> END_STONE_LAMP_ENABLED;

        public static final Supplier<Boolean> BLACKSTONE_LAMP_ENABLED;

        public static final Supplier<Boolean> DEEPSLATE_LAMP_ENABLED;

        public static final Supplier<Boolean> COPPER_LANTERN_ENABLED;

        public static final Supplier<Boolean> CHECKERBOARD_ENABLED;

        public static final Supplier<Boolean> NETHERITE_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> NETHERITE_DOOR_ENABLED;

        public static final Supplier<Boolean> PANCAKES_ENABLED;

        public static final Supplier<Boolean> CRIMSON_LANTERN_ENABLED;

        public static final Supplier<Boolean> TILE_ENABLED;

        public static final Supplier<Boolean> RAKED_GRAVEL_ENABLED;

        public static final Supplier<Boolean> STATUE_ENABLED;

        public static final Supplier<Boolean> FEATHER_BLOCK_ENABLED;

        public static final Supplier<Boolean> FLINT_BLOCK_ENABLED;

        public static final Supplier<Boolean> DOORMAT_ENABLED;

        public static final Supplier<Boolean> FLOWER_BOX_ENABLED;

        public static final Supplier<Boolean> BLACKSTONE_TILE_ENABLED;

        public static final Supplier<Boolean> SILVER_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> SILVER_DOOR_ENABLED;

        public static final Supplier<Boolean> LEAD_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> LEAD_DOOR_ENABLED;

        public static final Supplier<Boolean> LAPIS_BRICKS_ENABLED;

        public static final Supplier<Boolean> CANDLE_HOLDER_ENABLED;

    }


    public static class Utilities{
        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("utilities");

            builder.push("rope");
            ROPE_ENABLED = feature(builder);
            ROPE_UNRESTRICTED = builder.comment("Allows ropes to be supported & attached to solid block sides")
                    .define("block_side_attachment", true);
            ROPE_SLIDE = builder.comment("Makes sliding down ropes as fast as free falling, still negating fall damage")
                    .define("slide_on_fall", true);
            ROPE_OVERRIDE = builder.comment("In case you want to disable supplementaries ropes you can specify here another mod rope and they will be used for rope arrows and in mineshafts instead")
                    .define("rope_override", "supplementaries:rope");
            builder.pop();

            builder.push("jar");
            JAR_ENABLED = feature(builder);
            JAR_CAPACITY = builder.comment("Jar liquid capacity: leave at 12 for pixel accuracy")
                    .define("capacity", 12, 0, 1024);
            JAR_EAT = builder.comment("Allow right click to instantly eat or drink food or potions inside a placed jar.\n" +
                            "Disable if you think this ability is op (honey for example). Cookies are excluded")
                    .define("drink_from_jar", false);
            JAR_ITEM_DRINK = builder.comment("Allows the player to directly drink from jar items")
                    .define("drink_from_jar_item", false);
            JAR_AUTO_DETECT = builder.comment("Dynamically allows all small mobs inside jars depending on their hitbox size. Tinted jars can accept hostile mbos too")
                    .define("jar_auto_detect", false);
            JAR_CAPTURE = builder.comment("Allow Jars to capture small mobs")
                    .define("jar_capture", true);
            JAR_COOKIES = builder.comment("Allow Jars to hold cookies")
                    .define("jar_cookies", true);
            JAR_LIQUIDS = builder.comment("Allow Jars to hold liquids from bottles, buckets and bowls")
                    .define("jar_liquids", true);
            builder.pop();

            builder.push("hourglass");
            HOURGLASS_ENABLED = feature(builder);
            HOURGLASS_SUGAR = builder.comment("Time in ticks for sugar")
                    .define("sugar_time", 40, 0, 10000);
            HOURGLASS_SAND = builder.comment("Time in ticks for sand blocks")
                    .define("sand_time", 70, 0, 10000);
            HOURGLASS_CONCRETE = builder.comment("Time in ticks for concrete blocks")
                    .define("concrete_time", 105, 0, 10000);
            HOURGLASS_DUST = builder.comment("Time in ticks for generic dust")
                    .define("dust_time", 150, 0, 10000);
            HOURGLASS_GLOWSTONE = builder.comment("Time in ticks for glowstone dust")
                    .define("glowstone_time", 190, 0, 10000);
            HOURGLASS_BLAZE_POWDER = builder.comment("Time in ticks for blaze powder")
                    .define("blaze_powder_time", 277, 0, 10000);
            HOURGLASS_REDSTONE = builder.comment("Time in ticks for redstone dust")
                    .define("redstone_time", 400, 0, 10000);
            HOURGLASS_SLIME = builder.comment("Time in ticks for slime balls")
                    .define("slime_time", 1750, 0, 10000);
            HOURGLASS_HONEY = builder.comment("Time in ticks for honey")
                    .define("honey_time", 2000, 0, 10000);
            builder.pop();

            builder.push("cage");
            CAGE_ENABLED = feature(builder);
            CAGE_ALL_MOBS = builder.comment("Allows all entities to be captured by cages and jars. Not meant for survival")
                    .define("allow_all_mobs", false);
            CAGE_ALL_BABIES = builder.comment("Allows all baby mobs to be captured by cages")
                    .define("cage_allow_all_babies", false);
            CAGE_AUTO_DETECT = builder.comment("Dynamically allows all small mobs inside cages depending on their hitbox size")
                    .define("cage_auto_detect", false);
            CAGE_PERSISTENT_MOBS = builder.comment("Makes it so all (hostile) mobs captured by cages and jars will be set to persistent so they won't despawn when released")
                    .define("persistent_mobs", false);
            CAGE_HEALTH_THRESHOLD = builder.comment("Health percentage under which mobs will be allowed to be captured by cages and jars. Leave at 100 to accept any health level")
                    .define("health_threshold", 100, 1, 100);
            builder.pop();

            builder.push("safe");
            SAFE_ENABLED = feature(builder);
            SAFE_UNBREAKABLE = builder.comment("Makes safes only breakable by their owner or by a player in creative")
                    .define("prevent_breaking", false);
            SAFE_SIMPLE = builder.comment("Make safes simpler so they do not require keys:\n" +
                            "they will be bound to the first person that opens one and only that person will be able to interact with them")
                    .define("simple_safes", false);
            builder.pop();

            builder.push("sack");
            SACK_ENABLED = feature(builder);
            SACK_PENALTY = builder.comment("Penalize the player with slowness effect when carrying too many sacks")
                    .define("sack_penalty", true);
            SACK_INCREMENT = builder.comment("Maximum number of sacks after which the overencumbered effect will be applied. Each multiple of this number will increase the effect strength by one")
                    .define("sack_increment", 2, 0, 50);
            SACK_SLOTS = builder.comment("How many slots should a sack have")
                    .define("slots", 9, 1, 27);
            builder.pop();

            builder.push("bamboo_spikes");
            BAMBOO_SPIKES_ENABLED = feature(builder);
            TIPPED_SPIKES_ENABLED = feature(builder, "tipped_spikes");
            BAMBOO_SPIKES_DROP_LOOT = builder.comment("Allows entities killed by spikes to drop loot as if they were killed by a player")
                    .define("player_loot", false);
            BAMBOO_SPIKES_ALTERNATIVE = builder.comment("Alternative mode for bamboo spikes. Allows only harmful effects to be applied on them and they obtain infinite durability")
                    .define("alternative_mode", true);
            builder.pop();

            builder.push("urn");
            URN_ENABLED = feature(builder);
            URN_ENTITY_SPAWN_CHANCE = builder.comment("Chance for an urn to spawn a critter from the urn_spawn tag")
                    .define("critter_spawn_chance", 0.01f, 0, 1);
            builder.pop();

            builder.push("soap");
            SOAP_ENABLED = feature(builder);
            SOAP_DYE_CLEAN_BLACKLIST = builder.comment("Dyed Bock types that cannot be cleaned with soap")
                    .define("clean_blacklist", List.of("minecraft:glazed_terracotta"));
            builder.pop();

           FODDER_ENABLED = feature(builder, ModConstants.FODDER_NAME);
           PRESENT_ENABLED = feature(builder, ModConstants.PRESENT_NAME);
           FLAX_ENABLED = feature(builder, ModConstants.FLAX_NAME);

           builder.pop();
        }


        public static final Supplier<Boolean> SAFE_ENABLED;
        public static final Supplier<Boolean> SAFE_UNBREAKABLE;
        public static final Supplier<Boolean> SAFE_SIMPLE;

        public static final Supplier<Boolean> BAMBOO_SPIKES_ENABLED;
        public static final Supplier<Boolean> TIPPED_SPIKES_ENABLED;
        public static final Supplier<Boolean> BAMBOO_SPIKES_ALTERNATIVE;
        public static final Supplier<Boolean> BAMBOO_SPIKES_DROP_LOOT;

        public static final Supplier<Boolean> SACK_ENABLED;
        public static final Supplier<Boolean> SACK_PENALTY;
        public static final Supplier<Integer> SACK_INCREMENT;
        public static final Supplier<Integer> SACK_SLOTS;

        public static final Supplier<Boolean> HOURGLASS_ENABLED;
        public static final Supplier<Integer> HOURGLASS_DUST;
        public static final Supplier<Integer> HOURGLASS_SAND;
        public static final Supplier<Integer> HOURGLASS_CONCRETE;
        public static final Supplier<Integer> HOURGLASS_BLAZE_POWDER;
        public static final Supplier<Integer> HOURGLASS_GLOWSTONE;
        public static final Supplier<Integer> HOURGLASS_REDSTONE;
        public static final Supplier<Integer> HOURGLASS_SUGAR;
        public static final Supplier<Integer> HOURGLASS_SLIME;
        public static final Supplier<Integer> HOURGLASS_HONEY;

        public static final Supplier<Boolean> JAR_ENABLED;
        public static final Supplier<Integer> JAR_CAPACITY;
        public static final Supplier<Boolean> JAR_EAT;
        public static final Supplier<Boolean> JAR_CAPTURE;
        public static final Supplier<Boolean> JAR_COOKIES;
        public static final Supplier<Boolean> JAR_LIQUIDS;
        public static final Supplier<Boolean> JAR_ITEM_DRINK;
        public static final Supplier<Boolean> JAR_AUTO_DETECT;

        public static final Supplier<Boolean> CAGE_ENABLED;
        public static final Supplier<Boolean> CAGE_ALL_MOBS;
        public static final Supplier<Boolean> CAGE_ALL_BABIES;
        public static final Supplier<Boolean> CAGE_AUTO_DETECT;
        public static final Supplier<Boolean> CAGE_PERSISTENT_MOBS;
        public static final Supplier<Integer> CAGE_HEALTH_THRESHOLD;

        public static final Supplier<Boolean> SOAP_ENABLED;
        public static final Supplier<List<String>> SOAP_DYE_CLEAN_BLACKLIST;

        public static final Supplier<Boolean> ROPE_ENABLED;
        public static final Supplier<String> ROPE_OVERRIDE;
        public static final Supplier<Boolean> ROPE_UNRESTRICTED;
        public static final Supplier<Boolean> ROPE_SLIDE;

        public static final Supplier<Boolean> URN_ENABLED;
        public static final Supplier<Double> URN_ENTITY_SPAWN_CHANCE;

        public static final Supplier<Boolean> FODDER_ENABLED;

        public static final Supplier<Boolean> FLAX_ENABLED;



        public static final Supplier<Boolean> PRESENT_ENABLED;

    }


    public static class Tools {
        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("tools");

            builder.push("quiver");
            QUIVER_ENABLED = feature(builder);
            QUIVER_PREVENTS_SLOWS = builder.comment("Allows using a quiver without being slowed down")
                    .define("use_without_slow", true);
            QUIVER_SLOTS = builder.comment("Arrow stacks that can fit inside a quiver. Requires reboot")
                    .define("slots", 6, 1, 9);
            QUIVER_SKELETON_SPAWN = builder.comment("Increase this number to alter the probability for a Skeleton with quiver to spawn. Note that this also depends on local difficulty so you wont ever see them on easy and very rarely on normal. Similar logic to equipment")
                    .define("quiver_skeleton_chance", 0.2d, 0, 1);
            QUIVER_CURIO_ONLY = builder.comment("Allows quiver to only be used when in offhand or in curio slot")
                    .define("only_works_in_curio",false);
            QUIVER_PICKUP = builder.comment("Arrows you pickup will try to go in a quiver if available provided it has some arrow of the same type")
                    .define("quiver_pickup", true);
            builder.pop();

            builder.push("bubble_blower");
            BUBBLE_BLOWER_ENABLED = feature(builder);
            BUBBLE_BLOWER_COST = builder.comment("Amount of soap consumed per bubble block placed")
                    .define("stasis_cost", 5, 1, 25);
            builder.pop();

            builder.push("wrench");
            WRENCH_ENABLED = feature(builder);
            WRENCH_BYPASS = builder.comment("Allows wrenches to bypass a block interaction action prioritizing their own when on said hand")
                    .define("bypass_when_on", CommonConfigs.Hands.MAIN_HAND);
            builder.pop();

            //rope arrow
            builder.push("rope_arrow");
            ROPE_ARROW_ENABLED = feature(builder);
            ROPE_ARROW_CAPACITY = builder.comment("Max number of robe items allowed to be stored inside a rope arrow")
                    .define("capacity", 32, 1, 256);
            ROPE_ARROW_CROSSBOW = builder.comment("Makes rope arrows exclusive to crossbows")
                    .define("exclusive_to_crossbows", false);
            builder.pop();
            //flute
            builder.push("flute");
            FLUTE_ENABLED = feature(builder);
            FLUTE_RADIUS = builder.comment("Radius in which an unbound flute will search pets")
                    .define("unbound_radius", 64, 0, 500);
            FLUTE_DISTANCE = builder.comment("Max distance at which a bound flute will allow a pet to teleport")
                    .define("bound_distance", 64, 0, 500);

            builder.pop();

            builder.push("bomb");
            BOMB_ENABLED = feature(builder);
            BOMB_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .define("explosion_radius", 2, 0.1, 10);
            BOMB_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .define("break_blocks", BombEntity.BreakingMode.WEAK);
            BOMB_FUSE = builder.comment("Put here any number other than 0 to have your bombs explode after a certaom amount of ticks instead than on contact")
                    .define("bomb_fuse", 0, 0, 100000);
            builder.push("blue_bomb");
            BOMB_BLUE_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .define("explosion_radius", 5.15, 0.1, 10);
            BOMB_BLUE_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .define("break_blocks", BombEntity.BreakingMode.WEAK);
            //TODO: blue bomb config
            builder.pop();
            builder.pop();

            builder.push("slingshot");
            SLINGSHOT_ENABLED = feature(builder);
            SLINGSHOT_RANGE = builder.comment("Slingshot range multiplier. Affect the initial projectile speed")
                    .define("range_multiplier", 1f, 0, 5);
            SLINGSHOT_CHARGE = builder.comment("Time in ticks to fully charge a slingshot")
                    .define("charge_time", 20, 0, 100);
            SLINGSHOT_DECELERATION = builder.comment("Deceleration for the stasis projectile")
                    .define("stasis_deceleration", 0.9625, 0.1, 1);
            UNRESTRICTED_SLINGSHOT = builder.comment("Allow enderman to intercept any slingshot projectile")
                    .define("unrestricted_enderman_intercept", true);
            builder.pop();

            ANTIQUE_INK_ENABLED = feature(builder, ModConstants.ANTIQUE_INK_NAME);
            CANDY_ENABLED = feature(builder, ModConstants.CANDY_NAME);
            STASIS_ENABLED = feature(builder, ModConstants.STASIS_NAME);

            builder.pop();
        }


        public static final Supplier<Boolean> BUBBLE_BLOWER_ENABLED;
        public static final Supplier<Integer> BUBBLE_BLOWER_COST;

        public static final Supplier<Boolean> QUIVER_ENABLED;
        public static final Supplier<Boolean> QUIVER_PREVENTS_SLOWS;
        public static final Supplier<Integer> QUIVER_SLOTS;
        public static final Supplier<Double> QUIVER_SKELETON_SPAWN;
        public static final Supplier<Boolean> QUIVER_CURIO_ONLY;
        public static final Supplier<Boolean> QUIVER_PICKUP;

        public static final Supplier<Boolean> WRENCH_ENABLED;
        public static final Supplier<CommonConfigs.Hands> WRENCH_BYPASS;

        public static final Supplier<Boolean> SLINGSHOT_ENABLED;
        public static final Supplier<Double> SLINGSHOT_RANGE;
        public static final Supplier<Integer> SLINGSHOT_CHARGE;
        public static final Supplier<Double> SLINGSHOT_DECELERATION;
        public static final Supplier<Boolean> UNRESTRICTED_SLINGSHOT;

        public static final Supplier<Boolean> BOMB_ENABLED;
        public static final Supplier<Double> BOMB_RADIUS;
        public static final Supplier<Integer> BOMB_FUSE;
        public static final Supplier<BombEntity.BreakingMode> BOMB_BREAKS;
        public static final Supplier<Double> BOMB_BLUE_RADIUS;
        public static final Supplier<BombEntity.BreakingMode> BOMB_BLUE_BREAKS;

        public static final Supplier<Boolean> FLUTE_ENABLED;
        public static final Supplier<Integer> FLUTE_RADIUS;
        public static final Supplier<Integer> FLUTE_DISTANCE;

        public static final Supplier<Boolean> ROPE_ARROW_ENABLED;
        public static final Supplier<Integer> ROPE_ARROW_CAPACITY;
        public static final Supplier<Boolean> ROPE_ARROW_CROSSBOW;

        public static final Supplier<Boolean> ANTIQUE_INK_ENABLED;

        public static final Supplier<Boolean> CANDY_ENABLED;

        public static final Supplier<Boolean> STASIS_ENABLED;

    }
/*
    public static class Tweaks {
        public static final Supplier<Boolean> SHULKER_HELMET_ENABLED;
        public static final Supplier<Boolean> ENDERMAN_HEAD_ENABLED;


    }

    public static class Misc {


        public static final Supplier<Boolean> JAR_TAB;
        public static final Supplier<Boolean> CREATIVE_TAB;
        public static final Supplier<Boolean> DISPENSERS;
        public static final Supplier<Boolean> CUSTOM_CONFIGURED_SCREEN;
        public static final Supplier<Boolean> DEBUG_RESOURCES;
        public static final Supplier<Boolean> PACK_DEPENDANT_ASSETS;
    }
*/

    private static Supplier<Boolean> feature(ConfigBuilder builder) {
        return feature(builder, "enabled", true);
    }
    private static Supplier<Boolean> feature(ConfigBuilder builder, String name) {
        return feature(builder, name, true);
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder, String name, boolean value) {
        var config = builder.gameRestart().define(name, value);
        FEATURE_TOGGLES.put(name, config);
        return config;
    }
}
