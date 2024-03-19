package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.moonlight.api.ModSharedVariables;
import net.mehvahdjukaar.moonlight.api.client.anim.PendulumAnimation;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


public class ClientConfigs {


    public static void init() {
    }

    public static final ConfigSpec SPEC;

    static WeakReference<ConfigBuilder> builderReference;

    static {
        if (!PlatHelper.getPhysicalSide().isClient()) {
            String message = "Tried to load client configs on a dedicated server";
            Supplementaries.LOGGER.error(message);
            if (PlatHelper.isDev()) {
                throw new AssertionError(message);
            }
        }

        ConfigBuilder builder = ConfigBuilder.create(Supplementaries.res("client"), ConfigType.CLIENT);
        builderReference = new WeakReference<>(builder);

        Blocks.init();
        Particles.init();
        General.init();
        Tweaks.init();
        Items.init();

        builder.onChange(ClientConfigs::onChange);
        SPEC = builder.buildAndRegister();


        SPEC.loadFromFile();
    }

    private static void onChange() {
        Double b = ModSharedVariables.getDouble("color_multiplier");
        signColorMult = (float) (b == null ? 1 : b);
    }

    private static float signColorMult = 1;

    public static float getSignColorMult() {
        return signColorMult;
    }


    public static class Items {

        private static void init() {

        }

        public static final Supplier<QuiverLayer.QuiverMode> QUIVER_RENDER_MODE;
        public static final Supplier<QuiverLayer.QuiverMode> QUIVER_SKELETON_RENDER_MODE;
        public static final Supplier<Double> QUIVER_ARMOR_OFFSET;
        public static final Supplier<Boolean> QUIVER_MOUSE_MOVEMENT;
        public static final Supplier<Boolean> QUIVER_OVERLAY;
        public static final Supplier<Integer> QUIVER_GUI_X;
        public static final Supplier<Integer> QUIVER_GUI_Y;
        public static final Supplier<Boolean> SLINGSHOT_OVERLAY;
        public static final Supplier<Boolean> SLINGSHOT_OUTLINE;
        public static final Supplier<Integer> SLINGSHOT_OUTLINE_COLOR;
        public static final Supplier<Double> SLINGSHOT_PROJECTILE_SCALE;
        public static final Supplier<Boolean> WRENCH_PARTICLES;
        public static final Supplier<Boolean> FLUTE_PARTICLES;
        public static final Supplier<Boolean> DEPTH_METER_CLICK;
        public static final Supplier<Integer> DEPTH_METER_STEP_MULT;
        public static final Supplier<List<ResourceLocation>> DEPTH_METER_DIMENSIONS;

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("items");

            builder.push("slingshot");
            SLINGSHOT_OVERLAY = builder.comment("Adds an overlay to slingshots in gui displaying currently selected block")
                    .define("overlay", true);
            SLINGSHOT_OUTLINE = builder.comment("Render the block outline for distant blocks that are reachable with a slingshot enchanted with Stasis")
                    .define("stasis_block_outline", true);
            SLINGSHOT_OUTLINE_COLOR = builder.comment("An RGBA color for the block outline in hex format, for example 0x00000066 for vanilla outline colors")
                    .defineColor("block_outline_color", 0xffffff66);
            SLINGSHOT_PROJECTILE_SCALE = builder.comment("How big should a slingshot projectile look")
                    .define("projectile_scale", 0.5, 0, 1);
            builder.pop();

            builder.push("altimeter");
            DEPTH_METER_CLICK = builder.comment("Click action for depth meter which displays current depth")
                    .define("click_action", true);
            DEPTH_METER_DIMENSIONS = builder.comment("Allows depth meter to have unique textures per each dimension. Add more dimensions IDs and a matching texture in the correct path replacing ':' with '_'")
                    .defineObjectList("extra_dimension_textures", () -> List.of(Level.NETHER.location(), Level.END.location()), ResourceLocation.CODEC);
            DEPTH_METER_STEP_MULT = builder.comment("Increasing this to be more than 1 will result in delth meter display image to be shown in float amounts instead of pixel perfect ones")
                    .define("texture_precision_multiplier", 1, 1, 10);
            builder.pop();

            builder.push("quiver");
            QUIVER_ARMOR_OFFSET = builder.comment("Z offset for quiver render when wearing armor. Useful for when you have custom armor bigger than vanilla to void clipping. Leave at -1 for automatic offset")
                    .define("armor_render_offset", -1d, -1d, 1);
            QUIVER_RENDER_MODE = builder.comment("How quivers should render onto players")
                    .define("render_mode", QuiverLayer.QuiverMode.THIGH);
            QUIVER_SKELETON_RENDER_MODE = builder.comment("How skeleton with quivers should render it")
                    .define("skeleton_render_mode", QuiverLayer.QuiverMode.THIGH);
            QUIVER_OVERLAY = builder.comment("Adds an overlay to quivers in gui displaying currently selected arrow")
                    .define("overlay", true);
            QUIVER_MOUSE_MOVEMENT = builder.comment("Allows using your mouse to select an arrow in the quiver GUI")
                    .define("mouse_movement_in_gui", true);
            QUIVER_GUI_X = builder.comment("Quiver GUI X offset from default position")
                    .define("gui_x_offset", 0, -1000, 1000);
            QUIVER_GUI_Y = builder.comment("Quiver GUI Y offset from default position")
                    .define("gui_y_offset", 0, -1000, 1000);
            builder.pop();

            builder.push("wrench");
            WRENCH_PARTICLES = builder.comment("Display visual particles when a block is rotated")
                    .define("turn_particles", true);
            builder.pop();

            builder.push("flute");
            FLUTE_PARTICLES = builder.comment("Display visual particles when a playing a flute")
                    .define("note_particles", true);
            builder.pop();

            builder.pop();
        }
    }

    public static class Tweaks {

        private static void init() {
        }

        public static final Supplier<Boolean> COLORED_ARROWS;
        public static final Supplier<Boolean> COLORED_BREWING_STAND;
        public static final Supplier<Boolean> CLOCK_CLICK;
        public static final Supplier<Boolean> COMPASS_CLICK;
        public static final Supplier<Boolean> BOOK_GLINT;
        public static final Supplier<List<String>> BOOK_COLORS;
        public static final Supplier<Boolean> BANNER_PATTERN_TOOLTIP;
        public static final Supplier<Boolean> PAINTINGS_TOOLTIPS;
        public static final Supplier<Boolean> SHERDS_TOOLTIPS;
        public static final Supplier<Integer> TOOLTIP_IMAGE_SIZE;
        public static final Supplier<Boolean> MOB_HEAD_EFFECTS;
        public static final Supplier<Boolean> DEATH_CHAT;
        public static final Supplier<Boolean> TALL_GRASS_COLOR_CHANGE;
        public static final Supplier<Boolean> COLORED_MAPS;
        public static final Supplier<Boolean> ACCURATE_COLORED_MAPS;


        static {
            ConfigBuilder builder = builderReference.get();

            builder.comment("Game tweaks")
                    .push("tweaks");
            COLORED_BREWING_STAND = builder.comment("Colors the brewing stand potion texture depending on the potions it's brewing.\n" +
                            "If using a resource pack add tint index from 0 to 3 to the 3 potion layers")
                    .define("brewing_stand_colors", true);
            COLORED_ARROWS = builder.comment("Makes tipped arrows show their colors when loaded with a crossbow")
                    .define("crossbows_colors", true);
            CLOCK_CLICK = builder.comment("Allow to right click with a clock to display current time in numerical form")
                    .define("clock_right_click", true);
            COMPASS_CLICK = builder.comment("Allow to right click with a compass to display current coordinates in numerical form")
                    .define("compass_right_click", false);
            BOOK_GLINT = builder.comment("Renders an enchantment glint on placeable enchanted books" +
                            "Note that turning this on will make book piles use tile renderer instead of baked models making them slower to render")
                    .define("placeable_books_glint", false);
            BOOK_COLORS = builder.comment("Placeable books random colors")
                    .define("placeable_books_random_colors", BookPileBlockTile.DEFAULT_COLORS);
            BANNER_PATTERN_TOOLTIP = builder.comment("Enables banner pattern tooltip image preview")
                    .define("banner_pattern_tooltip", true);
            PAINTINGS_TOOLTIPS = builder.comment("Enables paintings tooltip image preview")
                    .define("paintings_tooltip", true);
            SHERDS_TOOLTIPS = builder.comment("Enables sherds tooltip image preview")
                    .define("sherds_tooltip", true);
            TOOLTIP_IMAGE_SIZE = builder.comment("Size of the tooltip image used for Sherds, Blackboards, Banner patterns and Paintings")
                    .define("tooltip_image_size", 80, 1, 255);
            MOB_HEAD_EFFECTS = builder.comment("Wearing mob heads will apply post processing")
                    .define("mob_head_shaders", true);
            DEATH_CHAT = builder.comment("Sends your current chat when you die while typing")
                    .define("send_chat_on_death", true);
            builder.push("colored_maps");
            COLORED_MAPS = builder
                    .comment("Needs the server config with same name on. If on here it will ignore the server one and keep vanilla colors")
                    .define("tinted_blocks_on_maps", true);
            TALL_GRASS_COLOR_CHANGE = builder.comment("Colors tall grass same color as grass")
                    .define("tall_grass_color", true);
            ACCURATE_COLORED_MAPS = builder.comment("Makes colored maps a bit more accurate. Might affect performance")
                            .define("accurate_colors", false);
            builder.pop();
            builder.pop();
        }
    }

    public static class General {

        private static void init() {
        }

        public static final Supplier<Boolean> CONFIG_BUTTON;
        public static final Supplier<Integer> CONFIG_BUTTON_Y_OFF;
        public static final Supplier<Boolean> TOOLTIP_HINTS;
        public static final Supplier<Boolean> PLACEABLE_TOOLTIP;
        public static final Supplier<Boolean> CUSTOM_CONFIGURED_SCREEN;
        public static final Supplier<Boolean> NO_OPTIFINE_WARN;
        public static final Supplier<Boolean> NO_AMENDMENTS_WARN;

        public static final Supplier<Double> TEST1;
        public static final Supplier<Double> TEST2;
        public static final Supplier<Double> TEST3;

        static {
            ConfigBuilder builder = builderReference.get();

            builder.comment("General settings")
                    .push("general");
            NO_OPTIFINE_WARN = builder.comment("Disables Optifine warn screen")
                    .define("no_optifine_warn_screen", false);
            NO_AMENDMENTS_WARN = builder.comment("Disables Amendments suggestion screen")
                    .define("no_amendments_screen", false);
            CONFIG_BUTTON = builder.comment("Enable Quark style config button on main menu. Needs Configured installed to work")
                    .define("config_button", CompatHandler.CONFIGURED);
            CONFIG_BUTTON_Y_OFF = builder.comment("Config button Y offset")
                    .define("config_button_y_offset", 0, -10000, 10000);
            TOOLTIP_HINTS = builder.comment("Show some tooltip hints to guide players through the mod")
                    .define("tooltip_hints", true);
            PLACEABLE_TOOLTIP = builder.comment("Show tooltips items that have been made placeable")
                    .define("placeable_tooltips", true);
            CUSTOM_CONFIGURED_SCREEN = builder.comment("Enables custom Configured config screen")
                    .define("custom_configured_screen", true);
            TEST1 = builder.comment("ignore this").define("test1", 0f, -10, 10);
            TEST2 = builder.comment("ignore this").define("test2", 0f, -10, 10);
            TEST3 = builder.comment("ignore this").define("test3", 0f, -10, 10);
            builder.pop();
        }
    }

    public enum GraphicsFanciness {
        FAST,
        FANCY,
        FABULOUS
    }

    public static class Blocks {

        private static void init() {
        }

        public static final Supplier<Double> BUBBLE_BLOCK_WOBBLE;
        public static final Supplier<Double> BUBBLE_BLOCK_GROW_SPEED;
        public static final Supplier<Boolean> PEDESTAL_SPIN;
        public static final Supplier<Boolean> PEDESTAL_SPECIAL;
        public static final Supplier<Double> PEDESTAL_SPEED;
        public static final Supplier<Boolean> SHELF_TRANSLATE;
        public static final Supplier<Double> WIND_VANE_POWER_SCALING;
        public static final Supplier<Double> WIND_VANE_ANGLE_1;
        public static final Supplier<Double> WIND_VANE_ANGLE_2;
        public static final Supplier<Double> WIND_VANE_PERIOD_1;
        public static final Supplier<Double> WIND_VANE_PERIOD_2;
        public static final Supplier<Boolean> CLOCK_24H;
        public static final Supplier<Boolean> GLOBE_RANDOM;
        public static final Supplier<Boolean> GLOBE_COORDINATES;

        public static final Supplier<GraphicsFanciness> FLAG_FANCINESS;
        public static final Supplier<Boolean> FLAG_BANNER;
        public static final Supplier<Integer> FLAG_PERIOD;
        public static final Supplier<Double> FLAG_WAVELENGTH;
        public static final Supplier<Double> FLAG_AMPLITUDE;
        public static final Supplier<Double> FLAG_AMPLITUDE_INCREMENT;
        public static final Supplier<List<String>> TICKABLE_MOBS;

        public static final Supplier<Boolean> NOTICE_BOARD_CENTERED_TEXT;
        public static final Supplier<Boolean> FAST_BUNTINGS;

        public static final Supplier<PendulumAnimation.Config> HAT_STAND_CONFIG;

        public static final Supplier<Boolean> TURN_TABLE_PARTICLES;
        public static final Supplier<Boolean> SPEAKER_BLOCK_MUTE;
        public static final Supplier<Double> ROPE_WOBBLE_AMPLITUDE;
        public static final Supplier<Double> ROPE_WOBBLE_PERIOD;

        static {

            ConfigBuilder builder = builderReference.get();

            builder.comment("""
                            Tweak and change the various block animations.
                            Only cosmetic stuff in here so to leave default if not interested.
                            Remember to delete this and server configs and let it refresh every once in a while since I might have tweaked it""")
                    .push("blocks");

            builder.push("globe");
            GLOBE_RANDOM = builder.comment("Enable a random globe texture for each world").define("random_world", true);
            GLOBE_COORDINATES = builder.comment("Displays current coordinates when using a globe").define("show_coordinates", true);
            builder.pop();

            builder.push("notice_board");
            NOTICE_BOARD_CENTERED_TEXT = builder.comment("Allows notice board displayed text to be centered instead of being left aligned")
                    .define("centered_text", true);
            builder.pop();

            builder.push("bunting");
            FAST_BUNTINGS = builder.comment("Makes buntings use normal block models with no animation for faster performance. When off this is only active when viewed from a distance")
                    .define("fast_buntings", false);
            builder.pop();

            builder.push("clock_block");
            CLOCK_24H = builder.comment("Display 24h time format. False for 12h format").define("24h_format", true);
            builder.pop();

            builder.push("pedestal");
            PEDESTAL_SPIN = builder.comment("Enable displayed item spin")
                    .define("spin", true);
            PEDESTAL_SPEED = builder.comment("Spin speed")
                    .define("speed", 2.0, 0, 100);
            PEDESTAL_SPECIAL = builder.comment("Enable special display types for items like swords, tridents or end crystals")
                    .define("fancy_renderers", true);
            builder.pop();

            builder.push("bubble_block");
            BUBBLE_BLOCK_WOBBLE = builder.comment("Wobbling intensity. set to 0 to disable")
                    .define("wobble", 0.2, 0, 1);
            BUBBLE_BLOCK_GROW_SPEED = builder.comment("How fast it grows when created. 1 to be instant")
                    .define("grow_speed", 0.4, 0, 1);
            builder.pop();

            builder.push("item_shelf");
            SHELF_TRANSLATE = builder.comment("Translate down displayed 3d blocks so that they are touching the shelf.\n" +
                            "Note that they will not be centered vertically this way")
                    .define("supported_blocks", true);
            builder.pop();

            builder.push("wind_vane");
            WIND_VANE_POWER_SCALING = builder.comment("""
                            Wind vane animation swings according to this equation:\s
                            angle(time) = max_angle_1*sin(2pi*time*pow/period_1) + <max_angle_2>*sin(2pi*time*pow/<period_2>)
                            where:
                             - pow = max(1,redstone_power*<power_scaling>)
                             - time = time in ticks
                             - redstone_power = block redstone power
                            <power_scaling> = how much frequency changes depending on power. 2 means it spins twice as fast each power level (2* for rain, 4* for thunder)
                            increase to have more distinct indication when weather changes""")
                    .define("power_scaling", 3.0, 1.0, 100.0);
            WIND_VANE_ANGLE_1 = builder.comment("Amplitude (maximum angle) of first sine wave")
                    .define("max_angle_1", 30.0, 0, 360);
            WIND_VANE_ANGLE_2 = builder.define("max_angle_2", 10.0, 0, 360);
            WIND_VANE_PERIOD_1 = builder.comment("Base period in ticks at 0 power of first sine wave")
                    .define("period_1", 450.0, 0.0, 2000.0);
            WIND_VANE_PERIOD_2 = builder.comment("This should be kept period_1/3 for a symmetric animation")
                    .define("period_2", 150.0, 0.0, 2000.0);
            builder.pop();

            builder.push("flag");
            FLAG_PERIOD = builder.comment("How slow a flag will oscillate. (Period of oscillation)\n" +
                            "Lower value = faster oscillation")
                    .define("slowness", 100, 0, 10000);
            FLAG_WAVELENGTH = builder.comment("How wavy the animation will be in pixels. (Wavelength)")
                    .define("wavyness", 4d, 0.001, 100);
            FLAG_AMPLITUDE = builder.comment("How tall the wave lobes will be. (Wave amplitude)")
                    .define("intensity", 1d, 0d, 100d);
            FLAG_AMPLITUDE_INCREMENT = builder.comment("How much the wave amplitude increases each pixel. (Amplitude increment per pixel)")
                    .define("intensity_increment", 0.3d, 0, 10);
            FLAG_FANCINESS = builder.comment("At which graphic settings flags will have a fancy renderer: 0=fast, 1=fancy, 2=fabulous")
                    .define("fanciness", GraphicsFanciness.FABULOUS);
            FLAG_BANNER = builder.comment("Makes flags render as sideways banner. Ignores many of the previously defined configs")
                    .define("render_as_banner", false);
            builder.pop();

            builder.push("captured_mobs").comment("THIS IS ONLY FOR VISUALS! To allow more entities in cages you need to edit the respective tags!");

            TICKABLE_MOBS = builder.comment("A list of mobs that can be ticked on client side when inside jars. Mainly used for stuff that has particles. Can cause issues and side effects so use with care")
                    .define("tickable_inside_jars", Arrays.asList("iceandfire:pixie", "druidcraft:dreadfish", "druidcraft:lunar_moth", "alexsmobs:hummingbird"));

            builder.pop();

            builder.push("hat_stand");
            HAT_STAND_CONFIG = builder.defineObject("swing_physics",
                    () -> new PendulumAnimation.Config(0, 55, 1.625f, 1.5f, true, 1.5f, 15),
                    PendulumAnimation.Config.CODEC);
            builder.pop();

            builder.push("turn_table");
            TURN_TABLE_PARTICLES = builder.comment("Display visual particles when a block is rotated")
                    .define("turn_particles", true);
            builder.pop();

            builder.push("speaker_block");
            SPEAKER_BLOCK_MUTE = builder.comment("Mute speaker block incoming narrator messages and displays them in chat instead")
                    .define("mute_narrator", false);
            builder.pop();

            builder.push("rope");
            ROPE_WOBBLE_AMPLITUDE = builder.comment("Amplitude of rope wobbling effect")
                    .define("wobbling_amplitude", 1.2d, 0, 20);
            ROPE_WOBBLE_PERIOD = builder.comment("Period of rope wobbling effect")
                    .define("wobbling_period", 12d, 0.01, 200);
            builder.pop();

            builder.pop();
        }
    }


    public static class Particles {

        private static void init() {
        }

        public static final Supplier<Integer> TURN_INITIAL_COLOR;
        public static final Supplier<Integer> TURN_FADE_COLOR;

        static {

            ConfigBuilder builder = builderReference.get();
            builder.comment("Particle parameters")
                    .push("particles");


            builder.comment("Rotation particle")
                    .push("turn_particle");

            TURN_INITIAL_COLOR = builder.comment("An RGBA color")
                    .defineColor("initial_color", 0x2a77ea);
            TURN_FADE_COLOR = builder.comment("An RGBA color")
                    .defineColor("fade_color", 0x32befa);

            builder.pop();

            builder.pop();
        }
    }

}
