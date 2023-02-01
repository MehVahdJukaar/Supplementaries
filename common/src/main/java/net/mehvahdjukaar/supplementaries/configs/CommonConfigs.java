package net.mehvahdjukaar.supplementaries.configs;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.mixins.MineshaftCorridorMixin;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class CommonConfigs {

    public static void init() {
    }


    public static final ConfigSpec SERVER_SPEC;

    private static final WeakReference<ConfigBuilder> builderReference;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Supplementaries.res("common"), ConfigType.COMMON);

        builderReference = new WeakReference<>(builder);

        Blocks.init();
        Spawns.init();
        Tweaks.init();
        Items.init();
        General.init();

        builder.setSynced();
        builder.onChange(CommonConfigs::onRefresh);

        SERVER_SPEC = builder.buildAndRegister();
        SERVER_SPEC.loadFromFile();
    }

    private static void onRefresh() {
        //this isn't safe. refresh could happen sooner than item registration for fabric
        Blocks.ropeOverride = Suppliers.memoize(() -> {
            var o = Registry.BLOCK.getHolder(ResourceKey.create(Registry.BLOCK.key(),
                    new ResourceLocation(Blocks.ROPE_OVERRIDE.get())));
            if (o.isPresent() && o.get() instanceof Holder.Reference<Block> hr && hr.value() != ModRegistry.ROPE.get()) {
                return hr;
            }
            return null;
        });
    }

    @Nullable
    public static Block getSelectedRope() {
        var override = getRopeOverride();
        if (override != null) return override.value();
        else if (RegistryConfigs.ROPE_ENABLED.get()) return ModRegistry.ROPE.get();
        return null;
    }

    @Nullable
    public static Holder.Reference<Block> getRopeOverride() {
        return Blocks.ropeOverride.get();
    }

    public static class Items {

        private static void init() {
        }

        public static final Supplier<Integer> ROPE_ARROW_CAPACITY;
        public static final Supplier<Boolean> ROPE_ARROW_CROSSBOW;

        public static final Supplier<Integer> FLUTE_RADIUS;
        public static final Supplier<Integer> FLUTE_DISTANCE;
        public static final Supplier<Double> BOMB_RADIUS;
        public static final Supplier<Integer> BOMB_FUSE;
        public static final Supplier<BombEntity.BreakingMode> BOMB_BREAKS;
        public static final Supplier<Double> BOMB_BLUE_RADIUS;
        public static final Supplier<BombEntity.BreakingMode> BOMB_BLUE_BREAKS;
        public static final Supplier<Double> SLINGSHOT_RANGE;
        public static final Supplier<Integer> SLINGSHOT_CHARGE;
        public static final Supplier<Double> SLINGSHOT_DECELERATION;
        public static final Supplier<Boolean> UNRESTRICTED_SLINGSHOT;
        public static final Supplier<Hands> WRENCH_BYPASS;
        public static final Supplier<Boolean> QUIVER_PREVENTS_SLOWS;
        public static final Supplier<Integer> QUIVER_SLOTS;
        public static final Supplier<Double> QUIVER_SKELETON_SPAWN;
        public static final Supplier<Boolean> QUIVER_CURIO_ONLY;
        public static final Supplier<Boolean> QUIVER_PICKUP;
        public static final Supplier<Integer> BUBBLE_BLOWER_COST;
        public static final Supplier<List<String>> SOAP_DYE_CLEAN_BLACKLIST;


        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("items");

            builder.push("quiver");
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
            BUBBLE_BLOWER_COST = builder.comment("Amount of soap consumed per bubble block placed")
                    .define("stasis_cost", 5, 1, 25);
            builder.pop();

            builder.push("soap");
            SOAP_DYE_CLEAN_BLACKLIST = builder.comment("Dyed Bock types that cannot be cleaned with soap")
                    .define("clean_blacklist", List.of("minecraft:glazed_terracotta"));
            builder.pop();

            builder.push("wrench");
            WRENCH_BYPASS = builder.comment("Allows wrenches to bypass a block interaction action prioritizing their own when on said hand")
                    .define("bypass_when_on", Hands.MAIN_HAND);
            builder.pop();

            //rope arrow
            builder.push("rope_arrow");
            ROPE_ARROW_CAPACITY = builder.comment("Max number of robe items allowed to be stored inside a rope arrow")
                    .define("capacity", 32, 1, 256);
            ROPE_ARROW_CROSSBOW = builder.comment("Makes rope arrows exclusive to crossbows")
                    .define("exclusive_to_crossbows", false);
            builder.pop();
            //flute
            builder.push("flute");
            FLUTE_RADIUS = builder.comment("Radius in which an unbound flute will search pets")
                    .define("unbound_radius", 64, 0, 500);
            FLUTE_DISTANCE = builder.comment("Max distance at which a bound flute will allow a pet to teleport")
                    .define("bound_distance", 64, 0, 500);

            builder.pop();
            //bomb
            builder.push("bomb");
            BOMB_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .define("explosion_radius", 2, 0.1, 10);
            BOMB_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .define("break_blocks", BombEntity.BreakingMode.WEAK);
            BOMB_FUSE = builder.comment("Put here any number other than 0 to have your bombs explode after a certaom amount of ticks instead than on contact")
                    .define("bomb_fuse", 0, 0, 100000);
            builder.pop();

            builder.push("blue_bomb");
            BOMB_BLUE_RADIUS = builder.comment("Bomb explosion radius (damage depends on this)")
                    .define("explosion_radius", 5.15, 0.1, 10);
            BOMB_BLUE_BREAKS = builder.comment("Do bombs break blocks like tnt?")
                    .define("break_blocks", BombEntity.BreakingMode.WEAK);

            builder.pop();

            builder.push("slingshot");
            SLINGSHOT_RANGE = builder.comment("Slingshot range multiplier. Affect the initial projectile speed")
                    .define("range_multiplier", 1f, 0, 5);
            SLINGSHOT_CHARGE = builder.comment("Time in ticks to fully charge a slingshot")
                    .define("charge_time", 20, 0, 100);
            SLINGSHOT_DECELERATION = builder.comment("Deceleration for the stasis projectile")
                    .define("stasis_deceleration", 0.9625, 0.1, 1);
            UNRESTRICTED_SLINGSHOT = builder.comment("Allow enderman to intercept any slingshot projectile")
                    .define("unrestricted_enderman_intercept", true);
            builder.pop();


            builder.pop();
        }
    }

    public enum Hands {
        MAIN_HAND, OFF_HAND, BOTH, NONE
    }

    public static class Tweaks {

        private static void init() {
        }

        public static final Supplier<Boolean> ENDER_PEAR_DISPENSERS;
        public static final Supplier<Boolean> AXE_DISPENSER_BEHAVIORS;
        public static final Supplier<Boolean> DIRECTIONAL_CAKE;
        public static final Supplier<Boolean> DOUBLE_CAKE_PLACEMENT;
        public static final Supplier<Boolean> HANGING_POT_PLACEMENT;
        public static final Supplier<Boolean> WALL_LANTERN_PLACEMENT;
        public static final Supplier<Boolean> WALL_LANTERN_HIGH_PRIORITY;
        public static final Supplier<Boolean> THROWABLE_BRICKS_ENABLED;
        public static final Supplier<List<String>> WALL_LANTERN_BLACKLIST;
        public static final Supplier<LightableLanternBlock.FallMode> FALLING_LANTERNS;
        public static final Supplier<Boolean> BELL_CHAIN;
        public static final Supplier<Integer> BELL_CHAIN_LENGTH;
        public static final Supplier<Boolean> PLACEABLE_STICKS;
        public static final Supplier<Boolean> PLACEABLE_RODS;
        public static final Supplier<Boolean> RAKED_GRAVEL;
        public static final Supplier<Boolean> BOTTLE_XP;
        public static final Supplier<Integer> BOTTLING_COST;
        public static final Supplier<Boolean> RANDOM_ADVENTURER_MAPS;
        public static final Supplier<Boolean> MAP_MARKERS;
        public static final Supplier<Boolean> CEILING_BANNERS;
        public static final Supplier<Boolean> PLACEABLE_BOOKS;
        public static final Supplier<Boolean> WRITTEN_BOOKS;
        public static final Supplier<Double> BOOK_POWER;
        public static final Supplier<Double> ENCHANTED_BOOK_POWER;
        public static final Supplier<Boolean> ZOMBIE_HORSE_CONVERSION;
        public static final Supplier<Boolean> ZOMBIE_HORSE;
        public static final Supplier<Integer> ZOMBIE_HORSE_COST;
        public static final Supplier<Boolean> ZOMBIE_HORSE_UNDERWATER;
        public static final Supplier<Boolean> PLACEABLE_GUNPOWDER;
        public static final Supplier<Integer> GUNPOWDER_BURN_SPEED;
        public static final Supplier<Integer> GUNPOWDER_SPREAD_AGE;
        public static final Supplier<Boolean> MIXED_BOOKS;
        public static final Supplier<Boolean> SKULL_PILES;
        public static final Supplier<Boolean> SKULL_CANDLES;
        public static final Supplier<Boolean> SKULL_CANDLES_MULTIPLE;
        public static final Supplier<Boolean> WANDERING_TRADER_DOORS;

        static {
            ConfigBuilder builder = builderReference.get();

            builder.comment("Vanilla tweaks")
                    .push("tweaks");
            builder.push("traders_open_doors");
            WANDERING_TRADER_DOORS = builder.comment("Allows traders to open doors (because they couldnt aparently)")
                    .define("enabled", true);
            builder.pop();


            builder.push("dispenser_tweaks");
            AXE_DISPENSER_BEHAVIORS = builder.comment("Allows dispensers to use axes on blocks to strip logs and scrape off copper oxidation and wax")
                    .define("axe_strip", true);
            ENDER_PEAR_DISPENSERS = builder.comment("Enables shooting ender pearls with dispensers")
                    .define("shoot_ender_pearls", true);
            builder.pop();


            //double cake
            builder.push("cake_tweaks");
            DOUBLE_CAKE_PLACEMENT = builder.comment("Allows you to place a cake on top of another")
                    .define("double_cake", true);
            DIRECTIONAL_CAKE = builder.comment("Allows eating a cake from every side")
                    .define("directional_cake", true);
            builder.pop();

            //skulls stuff
            builder.push("mob_head_tweaks");
            SKULL_PILES = builder.comment("Allows you to place two mob heads on top of each other")
                    .define("skull_piles", true);
            SKULL_CANDLES = builder.comment("Allows candles to be placed on top of skulls")
                    .define("skull_candles", true);
            SKULL_CANDLES_MULTIPLE = builder.comment("Allows placing more than one candle ontop of each skull")
                    .define("multiple_candles", true);
            builder.pop();

            //hanging pot
            builder.push("hanging_flower_pots");
            HANGING_POT_PLACEMENT = builder.comment("allows you to place hanging flower pots. Works with any modded pot too")
                    .define("enabled", true);
            builder.pop();

            //throwable bricks
            builder.push("throwable_bricks");
            THROWABLE_BRICKS_ENABLED = builder.comment("Throw bricks at your foes! Might break glass blocks")
                    .define("enabled", true);
            builder.pop();

            //wall lantern
            builder.push("lantern_tweaks");
            WALL_LANTERN_PLACEMENT = builder.comment("Allow wall lanterns placement")
                    .define("enabled", true);

            WALL_LANTERN_HIGH_PRIORITY = builder.comment("Gives high priority to wall lantern placement. Enable to override other wall lanterns placements, disable if it causes issues with other mods that use lower priority block click events")
                    .define("high_priority", true);

            List<String> modBlacklist = Arrays.asList("extlights", "betterendforge", "tconstruct", "enigmaticlegacy");
            WALL_LANTERN_BLACKLIST = builder.comment("Mod ids of mods that have lantern block that extend the base lantern class but don't look like one")
                    .define("mod_blacklist", modBlacklist);
            FALLING_LANTERNS = builder.comment("Allows ceiling lanterns to fall if their support is broken." +
                            "Additionally if they fall from high enough they will break creating a fire where they land")
                    .define("fallin_lanterns", LightableLanternBlock.FallMode.ON);
            builder.pop();
            //bells
            builder.push("bells_tweaks");
            BELL_CHAIN = builder.comment("Ring a bell by clicking on a chain that's connected to it")
                    .define("chain_ringing", true);
            BELL_CHAIN_LENGTH = builder.comment("Max chain length that allows a bell to ring")
                    .define("chain_length", 16, 0, 256);
            builder.pop();

            builder.push("placeable_sticks");
            PLACEABLE_STICKS = builder.comment("Allow placeable sticks")
                    .define("sticks", true);
            PLACEABLE_RODS = builder.comment("Allow placeable blaze rods")
                    .define("blaze_rods", true);
            builder.pop();

            builder.push("placeable_gunpowder");
            PLACEABLE_GUNPOWDER = builder.comment("Allow placeable gunpowder")
                    .define("enabled", true);
            GUNPOWDER_BURN_SPEED = builder.comment("Number of ticks it takes for gunpowder to burn 1 stage (out of 8). Increase to slow it down")
                    .define("speed", 2, 0, 20);
            GUNPOWDER_SPREAD_AGE = builder.comment("Age at which it spread to the next gunpowder block. Also affects speed")
                    .define("spread_age", 2, 0, 8);
            builder.pop();

            builder.push("raked_gravel");
            RAKED_GRAVEL = builder.comment("allow gravel to be raked with a hoe")
                    .define("enabled", true);
            builder.pop();

            builder.push("bottle_xp");
            BOTTLE_XP = builder.comment("Allow bottling up xp by using a bottle on an enchanting table")
                    .define("enabled", false);
            BOTTLING_COST = builder.comment("bottling health cost")
                    .define("cost", 2, 0, 20);
            builder.pop();

            builder.push("map_tweaks");
            RANDOM_ADVENTURER_MAPS = builder.comment("Cartographers will sell 'adventurer maps' that will lead to a random vanilla structure (choosen from a thought out preset list).\n" +
                            "Best kept disabled if you are adding custom adventurer maps with datapack (check the wiki for more)")
                    .define("random_adventurer_maps", true);
            MAP_MARKERS = builder.comment("enables beacons, lodestones, respawn anchors, beds, conduits, portals to be displayed on maps by clicking one of them with a map")
                    .define("block_map_markers", true);
            builder.pop();

            builder.push("ceiling_banners");
            CEILING_BANNERS = builder.comment("Allow banners to be placed on ceilings")
                    .define("enabled", true);
            builder.pop();

            builder.push("placeable_books");
            WRITTEN_BOOKS = builder.comment("Allows written books to be placed down. Requires shift clicking")
                    .define("enabled", true);
            PLACEABLE_BOOKS = builder.comment("Allow books and enchanted books to be placed on the ground")
                    .define("enabled", true);
            BOOK_POWER = builder.comment("Enchantment power bonus given by normal book piles with 4 books. Piles with less books will have their respective fraction of this total. For reference a vanilla bookshelf provides 1")
                    .define("book_power", 1d, 0, 5);
            ENCHANTED_BOOK_POWER = builder.comment("Enchantment power bonus given by normal book piles with 4 books. Piles with less books will have their respective fraction of this total. For reference a vanilla bookshelf provides 1")
                    .define("enchanted_book_power", 1.334d, 0, 5);
            MIXED_BOOKS = builder.comment("Allow all books to be placed both vertically and horizontally")
                    .define("mixed_books", false);
            builder.pop();

            builder.push("zombie_horse");
            ZOMBIE_HORSE = builder.comment("Feed a stack of rotten flesh to a skeleton horse to buff him up to a zombie horse")
                    .define("zombie_horse_conversion", true);
            ZOMBIE_HORSE_COST = builder.comment("Amount of rotten flesh needed")
                    .define("rotten_flesh", 64, 1, 1000);
            ZOMBIE_HORSE_UNDERWATER = builder.comment("Allows zombie horses to be ridden underwater")
                    .define("rideable_underwater", true);
            ZOMBIE_HORSE_CONVERSION = builder.comment("Convert a zombie horse back by feeding it a golden carrot")
                    .define("zombie_horse_inverse_conversion", true);
            builder.pop();


            builder.pop();
        }

    }

    public static class General {

        private static void init() {
        }

        public static final Supplier<Boolean> SERVER_PROTECTION;

        static {
            ConfigBuilder builder = builderReference.get();

            builder.comment("General settings")
                    .push("general");
            SERVER_PROTECTION = builder.comment("Turn this on to disable any interaction on blocks placed by other players. This affects item shelves, signs, flower pots, and boards. " +
                            "Useful for protected servers. Note that it will affect only blocks placed after this is turned on and such blocks will keep being protected after this option is disabled")
                    .define("server_protection", false);

            builder.pop();
        }
    }

    public static class Blocks {

        private static void init() {
        }

        private static Supplier<Holder.Reference<Block>> ropeOverride = () -> null;
        public static final Supplier<String> ROPE_OVERRIDE;

        public static final Supplier<Double> URN_ENTITY_SPAWN_CHANCE;

        public static final Supplier<Boolean> BAMBOO_SPIKES_ALTERNATIVE;
        public static final Supplier<Boolean> BAMBOO_SPIKES_DROP_LOOT;

        public static final Supplier<Integer> BUBBLE_LIFETIME;
        public static final Supplier<Boolean> BUBBLE_BREAK;
        public static final Supplier<Boolean> BUBBLE_FEATHER_FALLING;

        public static final Supplier<Boolean> ROPE_UNRESTRICTED;
        public static final Supplier<Boolean> ROPE_SLIDE;
        public static final Supplier<Integer> GLOBE_TRADES;

        public static final Supplier<Integer> SPEAKER_RANGE;
        public static final Supplier<Boolean> SPEAKER_NARRATOR;

        public static final Supplier<Integer> BELLOWS_PERIOD;
        public static final Supplier<Integer> BELLOWS_POWER_SCALING;
        public static final Supplier<Double> BELLOWS_MAX_VEL;
        public static final Supplier<Double> BELLOWS_BASE_VEL_SCALING;
        public static final Supplier<Boolean> BELLOWS_FLAG;
        public static final Supplier<Integer> BELLOWS_RANGE;

        public static final Supplier<Double> LAUNCHER_VEL;
        public static final Supplier<Integer> LAUNCHER_HEIGHT;

        public static final Supplier<Boolean> TURN_TABLE_ROTATE_ENTITIES;

        public static final Supplier<Integer> JAR_CAPACITY;
        public static final Supplier<Boolean> JAR_EAT;
        public static final Supplier<Boolean> JAR_CAPTURE;
        public static final Supplier<Boolean> JAR_COOKIES;
        public static final Supplier<Boolean> JAR_LIQUIDS;
        public static final Supplier<Boolean> JAR_ITEM_DRINK;
        public static final Supplier<Boolean> JAR_AUTO_DETECT;
        public static final Supplier<Boolean> GOBLET_DRINK;
        public static final Supplier<Integer> CRYSTAL_ENCHANTING;

        public static final Supplier<Boolean> CAGE_ALL_MOBS;
        public static final Supplier<Boolean> CAGE_ALL_BABIES;
        public static final Supplier<Boolean> CAGE_AUTO_DETECT;
        public static final Supplier<Boolean> CAGE_PERSISTENT_MOBS;
        public static final Supplier<Integer> CAGE_HEALTH_THRESHOLD;

        public static final Supplier<Integer> SUGAR_BLOCK_HORSE_SPEED_DURATION;

        public static final Supplier<Boolean> NOTICE_BOARDS_UNRESTRICTED;

        public static final Supplier<Boolean> SACK_PENALTY;
        public static final Supplier<Integer> SACK_INCREMENT;
        public static final Supplier<Integer> SACK_SLOTS;

        public static final Supplier<Boolean> SAFE_UNBREAKABLE;
        public static final Supplier<Boolean> SAFE_SIMPLE;

        public static final Supplier<Boolean> BLACKBOARD_COLOR;
        public static final Supplier<BlackboardBlock.UseMode> BLACKBOARD_MODE;

        public static final Supplier<Boolean> REPLACE_DAUB;
        public static final Supplier<Boolean> SWAP_TIMBER_FRAME;
        public static final Supplier<Boolean> AXE_TIMBER_FRAME_STRIP;

        public static final Supplier<Integer> HOURGLASS_DUST;
        public static final Supplier<Integer> HOURGLASS_SAND;
        public static final Supplier<Integer> HOURGLASS_CONCRETE;
        public static final Supplier<Integer> HOURGLASS_BLAZE_POWDER;
        public static final Supplier<Integer> HOURGLASS_GLOWSTONE;
        public static final Supplier<Integer> HOURGLASS_REDSTONE;
        public static final Supplier<Integer> HOURGLASS_SUGAR;
        public static final Supplier<Integer> HOURGLASS_SLIME;
        public static final Supplier<Integer> HOURGLASS_HONEY;

        public static final Supplier<Boolean> ITEM_SHELF_LADDER;

        public static final Supplier<Boolean> DOUBLE_IRON_GATE;
        public static final Supplier<Boolean> CONSISTENT_GATE;

        public static final Supplier<Boolean> STICK_POLE;
        public static final Supplier<Integer> STICK_POLE_LENGTH;

        public static final Supplier<Boolean> ASH_BURN;
        public static final Supplier<Boolean> ASH_RAIN;

        public static final Supplier<Boolean> PLANTER_BREAKS;

        public static final Supplier<Integer> ENDERMAN_HEAD_INCREMENT;
        public static final Supplier<Boolean> ENDERMAN_HEAD_WORKS_FROM_ANY_SIDE;


        static {
            ConfigBuilder builder = builderReference.get();


            builder.comment("Server side blocks configs")
                    .push("blocks");

            builder.push("urn");
            URN_ENTITY_SPAWN_CHANCE = builder.comment("Chance for an urn to spawn a critter from the urn_spawn tag")
                    .define("critter_spawn_chance", 0.01f, 0, 1);
            builder.pop();

            builder.push("enderman_head");
            ENDERMAN_HEAD_INCREMENT = builder.comment("Time to increase 1 power level when being looked at")
                    .define("ticks_to_increase_power", 15, 0, 10000);
            ENDERMAN_HEAD_WORKS_FROM_ANY_SIDE = builder.comment("do enderman heads work when looked from any side?")
                    .define("work_from_any_side", false);
            builder.pop();

            builder.push("bamboo_spikes");
            BAMBOO_SPIKES_DROP_LOOT = builder.comment("Allows entities killed by spikes to drop loot as if they were killed by a player")
                    .define("player_loot", false);
            BAMBOO_SPIKES_ALTERNATIVE = builder.comment("Alternative mode for bamboo spikes. Allows only harmful effects to be applied on them and they obtain infinite durability")
                    .define("alternative_mode", true);
            builder.pop();

            builder.push("sugar_cube");
            SUGAR_BLOCK_HORSE_SPEED_DURATION = builder.comment("Duration in seconts of speed effect garanted to horses that eat a sugar cube")
                    .define("horse_speed_duration", 10, 0, 1000);
            builder.pop();

            builder.push("planter");
            PLANTER_BREAKS = builder.comment("Makes so saplings that grow in a planter will break it turning into rooted dirt")
                    .define("broken_by_sapling", true);
            builder.pop();

            builder.push("bubble_block");
            BUBBLE_LIFETIME = builder.comment("Max lifetime of bubble blocks. Set to 10000 to have it infinite")
                    .define("lifetime", 20 * 60, 1, 10000);
            BUBBLE_BREAK = builder.comment("Can bubble break when touched on?")
                    .define("break_when_touched", true);
            BUBBLE_FEATHER_FALLING = builder.comment("If true feather falling prevents breaking bubbles when stepping on them")
                    .define("feather_falling_prevents_breaking", true);
            builder.pop();

            builder.push("ash");
            ASH_BURN = builder.comment("Burnable blocks will have a chance to create ash layers when burned")
                    .define("ash_from_fire", true);
            ASH_RAIN = builder.comment("Allows rain to wash away ash layers overtime")
                    .define("rain_wash_ash", true);
            builder.pop();

            builder.push("rope");
            ROPE_UNRESTRICTED = builder.comment("Allows ropes to be supported & attached to solid block sides")
                    .define("block_side_attachment", true);
            ROPE_SLIDE = builder.comment("Makes sliding down ropes as fast as free falling, still negating fall damage")
                    .define("slide_on_fall", true);
            ROPE_OVERRIDE = builder.comment("In case you want to disable supplementaries ropes you can specify here another mod rope and they will be used for rope arrows and in mineshafts instead")
                    .define("rope_override", "supplementaries:rope");
            builder.pop();

            builder.push("pedestal");
            CRYSTAL_ENCHANTING = builder.comment("If enabled end crystals placed on a pedestals will provide an enchantment power bonus equivalent to 3 bookshelves")
                    .define("crystal_enchanting", 3, 0, 100);
            builder.pop();

            //globe
            builder.push("globe");
            GLOBE_TRADES = builder.comment("how many globe trades to give to the wandering trader. This will effectively increase the chance of him having a globe trader. Increase this if you have other mods that add stuff to that trader")
                    .define("chance", 2, 0, 50);
            builder.pop();

            //speaker
            builder.push("speaker_block");
            SPEAKER_NARRATOR = builder.comment("Enable/disable speaker block narrator mode")
                    .define("narrator_enabled", true);
            SPEAKER_RANGE = builder.comment("Maximum block range")
                    .define("range", 64, 0, 100000000);
            builder.pop();
            //bellows
            builder.push("bellows");
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
            //spring launcher
            builder.push("spring_launcher");
            LAUNCHER_VEL = builder.comment("spring launcher launch speed")
                    .define("velocity", 1.5D, 0, 16);
            LAUNCHER_HEIGHT = builder.comment("fall distance needed to trigger the automatic spring launch")
                    .define("fall_height_required", 5, 0, 512);
            builder.pop();
            //turn table
            builder.push("turn_table");
            TURN_TABLE_ROTATE_ENTITIES = builder.comment("can rotate entities standing on it?")
                    .define("rotate_entities", true);
            builder.pop();
            //jar
            builder.push("jar");
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

            //cage
            builder.push("cage");
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

            builder.push("goblet");
            GOBLET_DRINK = builder.comment("Allows drinking from goblets").define("allow_drinking", true);
            builder.pop();

            //notice boards
            builder.push("notice_board");
            NOTICE_BOARDS_UNRESTRICTED = builder.comment("Allows notice boards to accept and display any item, not just maps and books")
                    .define("allow_any_item", false);
            builder.pop();

            builder.push("sack");
            SACK_PENALTY = builder.comment("Penalize the player with slowness effect when carrying too many sacks")
                    .define("sack_penalty", true);
            SACK_INCREMENT = builder.comment("Maximum number of sacks after which the overencumbered effect will be applied. Each multiple of this number will increase the effect strength by one")
                    .define("sack_increment", 2, 0, 50);
            SACK_SLOTS = builder.comment("How many slots should a sack have")
                    .define("slots", 9, 1, 27);
            builder.pop();

            builder.push("safe");
            SAFE_UNBREAKABLE = builder.comment("Makes safes only breakable by their owner or by a player in creative")
                    .define("prevent_breaking", false);
            SAFE_SIMPLE = builder.comment("Make safes simpler so they do not require keys:\n" +
                            "they will be bound to the first person that opens one and only that person will be able to interact with them")
                    .define("simple_safes", false);
            builder.pop();

            builder.push("blackboard");
            BLACKBOARD_COLOR = builder.comment("Enable to draw directly on a blackboard using any dye. Gui still only works in black and white")
                    .define("colored_blackboard", PlatformHelper.isModLoaded("chalk"));
            BLACKBOARD_MODE = builder.comment("Interaction mode for blackboards")
                    .define("interaction_mode", BlackboardBlock.UseMode.BOTH);
            builder.pop();

            builder.push("timber_frame");
            REPLACE_DAUB = builder.comment("Replace a timber frame with wattle and daub block when daub is placed in it")
                    .define("replace_daub", true);
            SWAP_TIMBER_FRAME = builder.comment("Allow placing a timber frame directly on a block by holding shift")
                    .define("swap_on_shift", false);
            AXE_TIMBER_FRAME_STRIP = builder.comment("Allows axes to remove a framed block leaving the contained block intact")
                    .define("axes_strip", true);

            builder.pop();

            builder.push("hourglass");
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

            builder.push("item_shelf");
            ITEM_SHELF_LADDER = builder.comment("Makes item shelves climbable")
                    .define("climbable_shelves", false);
            builder.pop();

            builder.push("iron_gate");
            DOUBLE_IRON_GATE = builder.comment("Allows two iron gates to be opened simultaneously when on top of the other")
                    .define("double_opening", true);
            CONSISTENT_GATE = builder.comment("Makes iron (ang gold) gates behave like their door counterpart so for example iron gates will only be openeable by redstone")
                    .define("door-like_gates", false);
            builder.pop();

            builder.push("flag");
            STICK_POLE = builder.comment("Allows right/left clicking on a stick to lower/raise a flag attached to it")
                    .define("stick_pole", true);
            STICK_POLE_LENGTH = builder.comment("Maximum allowed pole length")
                    .define("pole_length", 16, 0, 256);
            builder.pop();

            builder.pop();

        }
    }

    public static class Spawns {

        private static void init() {
        }

        public static final Supplier<Boolean> DISTANCE_TEXT;
        public static final Supplier<Boolean> WAY_SIGN_ENABLED;

        public static final Supplier<Boolean> WILD_FLAX_ENABLED;
        public static final Supplier<Integer> FLAX_PATCH_TRIES;
        public static final Supplier<Integer> FLAX_AVERAGE_EVERY;

        public static final Supplier<Boolean> URN_PILE_ENABLED;
        public static final Supplier<Integer> URN_PATCH_TRIES;
        public static final Supplier<Integer> URN_PER_CHUNK;

        public static final Supplier<Boolean> BASALT_ASH_ENABLED;
        public static final Supplier<Integer> BASALT_ASH_TRIES;
        public static final Supplier<Integer> BASALT_ASH_PER_CHUNK;

        public static final Supplier<Double> RED_MERCHANT_SPAWN_MULTIPLIER;


        static {
            ConfigBuilder builder = builderReference.get();

            builder.comment("Configure spawning conditions")
                    .push("spawns");

            builder.push("way_sign");
            WAY_SIGN_ENABLED = builder.comment("Entirely disables them from spawning")
                    .define("enabled", true);
            DISTANCE_TEXT = builder.comment("With this option road signs will display the distance to the structure that they are pointing to")
                    .define("show_distance_text", true);

            builder.pop();

            builder.push("wild_flax");
            WILD_FLAX_ENABLED = builder.define("enabled", true);
            FLAX_AVERAGE_EVERY = builder.comment("Spawn wild flax on average every 'x' chunks. Increases spawn frequency")
                    .define("rarity", 6, 1, 100);
            FLAX_PATCH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 35, 1, 100);
            builder.pop();

            builder.push("cave_urns");
            URN_PILE_ENABLED = builder.define("enabled", true);
            URN_PATCH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 4, 1, 100);
            URN_PER_CHUNK = builder.comment("Spawn attempts per chunk. Increases spawn frequency")
                    .define("spawn_attempts", 7, 0, 100);
            builder.pop();

            builder.push("basalt_ash");
            BASALT_ASH_ENABLED = builder.define("enabled", true);
            BASALT_ASH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 36, 1, 1000);
            BASALT_ASH_PER_CHUNK = builder.comment("Spawn attempts per chunk. Increases spawn frequency")
                    .define("spawn_attempts", 15, 0, 100);
            builder.pop();

            builder.push("misc");
            RED_MERCHANT_SPAWN_MULTIPLIER = builder.comment("slightly increase this or decrease this number to tweak the red marchant spawn chance. Won't spawn at 0 and will spawn twice as often on 2")
                    .define("red_merchant_spawn_multiplier", 1d, 0, 10);
            builder.pop();

            builder.pop();
        }
    }

}