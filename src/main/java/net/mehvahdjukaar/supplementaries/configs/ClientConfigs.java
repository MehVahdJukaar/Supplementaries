package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.supplementaries.block.util.CapturedMobs;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ClientConfigs {
    public static ForgeConfigSpec CLIENT_CONFIG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        block.init(builder);
        particle.init(builder);
        entity.init(builder);
        general.init(builder);
        tweaks.init(builder);

        CLIENT_CONFIG = builder.build();
    }

    public static class tweaks {
        public static ForgeConfigSpec.BooleanValue COLORED_BWERING_STAND;
        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Game tweaks")
                    .push("tweaks");
            COLORED_BWERING_STAND = builder.comment("Colors the brewing stand potion texture depending on the potions it's brewing.\n"+
                    "If using a resource pack add tint index from 0 to 3 to the 3 potion layers")
                    .define("brewing_stand_colors",true);
            builder.pop();
        }
    }

    public static class general {
        public static ForgeConfigSpec.BooleanValue CONFIG_BUTTON;
        public static ForgeConfigSpec.BooleanValue TOOLTIP_HINTS;
        public static ForgeConfigSpec.BooleanValue ANTI_REPOST_WARNING;
        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("General settings")
                    .push("general");
            CONFIG_BUTTON = builder.comment("Enable Quark style config button on main menu. Needs Configured installed to work")
                    .define("config_button",false);
            TOOLTIP_HINTS = builder.comment("Show some tooltip hints to guide players through the mod")
                    .define("tooltip_hints",true);
            ANTI_REPOST_WARNING = builder.comment("Tries to detect when the mod hasn't been downloaded from Curseforge." +
                    "Set to false if you have manually changed the mod jar name")
                    .define("anti_reposting_warning", true);
            builder.pop();
        }
    }

    public static class block {
        public static ForgeConfigSpec.DoubleValue FIREFLY_SPAWN_CHANCE;
        public static ForgeConfigSpec.IntValue FIREFLY_SPAWN_PERIOD;

        public static ForgeConfigSpec.BooleanValue PEDESTAL_SPIN;
        public static ForgeConfigSpec.BooleanValue PEDESTAL_SPECIAL;
        public static ForgeConfigSpec.DoubleValue PEDESTAL_SPEED;
        public static ForgeConfigSpec.BooleanValue SHELF_TRANSLATE;
        public static ForgeConfigSpec.DoubleValue WIND_VANE_POWER_SCALING;
        public static ForgeConfigSpec.DoubleValue WIND_VANE_ANGLE_1;
        public static ForgeConfigSpec.DoubleValue WIND_VANE_ANGLE_2;
        public static ForgeConfigSpec.DoubleValue WIND_VANE_PERIOD_1;
        public static ForgeConfigSpec.DoubleValue WIND_VANE_PERIOD_2;
        public static ForgeConfigSpec.BooleanValue CLOCK_24H;
        public static ForgeConfigSpec.BooleanValue GLOBE_RANDOM;
        public static ForgeConfigSpec.ConfigValue<List<? extends List<String>>> GLOBE_COLORS;

        public static ForgeConfigSpec.IntValue FLAG_FANCINESS;
        public static ForgeConfigSpec.IntValue FLAG_PERIOD;
        public static ForgeConfigSpec.DoubleValue FLAG_WAVELENGTH;
        public static ForgeConfigSpec.DoubleValue FLAG_AMPLITUDE;
        public static ForgeConfigSpec.DoubleValue FLAG_AMPLITUDE_INCREMENT;
        public static ForgeConfigSpec.ConfigValue<List<? extends List<String>>> CAPTURED_MOBS_PROPERTIES;


        //is there a way to store more complicated data structures inside Forge configs. For example I would like to store a list containing many lists made up of lets say a string and an int value

        private static void init(ForgeConfigSpec.Builder builder) {

            builder.comment("Tweak and change the various block animations.\n" +
                    "Only cosmetic stuff in here so to leave default if not interested.\n"+
                    "Remember to delete this and server configs and let it refresh every once in a while since I might have tweaked it")
                    .push("blocks");

            builder.push("globe");
            GLOBE_RANDOM = builder.comment("Enable a random globe texture for each world").define("random_world", true);

            GLOBE_COLORS = builder.comment("Here you can put custom colors that will be assigned to each globe depending on the dimension where its placed:\n" +
                    "To do so you'll have to make a list for one entry for every dimension you want to recolor as follows:\n"+
                    "[[<id>,<c1>,...,<c12>],[<id>,<c1>,...,<c12>],...]\n"+
                    "With the following description:\n"+
                    " - <id> being the dimension id (ie: minecraft:the_nether)\n"+
                    " - <c1> to <c12> will have to be 12 hex colors (without the #) that will represent each of the 17 globe own 'virtual biome'\n"+
                    "Following are the virtual biomes that each index is associated with:\n"+
                    " - 1: water light\n"+
                    " - 2: water medium\n"+"" +
                    " - 3: water dark\n"+
                    " - 4: coast/taiga\n"+
                    " - 5: forest\n"+
                    " - 6: plains\n"+
                    " - 7: savanna\n"+
                    " - 8: desert\n"+
                    " - 9: snow\n"+
                    " - 10: ice\n"+
                    " - 11: iceberg/island\n"+
                    " - 12: mushroom island")
                    .defineList("globe_colors", GlobeTextureManager.GlobeColors.getDefaultConfig(), s->true);

            builder.pop();

            builder.push("clock_block");
            CLOCK_24H = builder.comment("Display 24h time format. False for 12h format").define("24h_format", true);
            builder.pop();

            builder.push("firefly_jar");
            FIREFLY_SPAWN_PERIOD = builder.comment("Particle in firefly jars spawn as explained below:\n"+
                    "Every <spawn_period> ticks a particle has a chance to spawn determined by <spawn_chance>x100 %.")
                    .defineInRange("spawn_period",8, 1, 20);
            FIREFLY_SPAWN_CHANCE = builder.comment("Spawn chance every period")
                    .defineInRange("spawn_chance", 0.3, 0, 1);
            builder.pop();

            builder.push("pedestal");
            PEDESTAL_SPIN = builder.comment("Enable displayed item spin")
                    .define("spin",true);
            PEDESTAL_SPEED = builder.comment("Spin speed")
                    .defineInRange("speed",2f,0f,100f);
            PEDESTAL_SPECIAL = builder.comment("Enable special display types for items like swords, tridents or end crystals")
                    .define("fancy_renderers",true);
            builder.pop();

            builder.push("item_shelf");
            SHELF_TRANSLATE = builder.comment("Translate down displayed 3d blocks so that they are touching the shelf.\n"+
                    "Note that they will not be centered vertically this way")
                    .define("supported_blocks", true);
            builder.pop();

            builder.push("wind_vane").comment();
            WIND_VANE_POWER_SCALING = builder.comment("Wind vane animation swings according to this equation: \n"+
                    "angle(time) = max_angle_1*sin(2pi*time*pow/period_1) + <max_angle_2>*sin(2pi*time*pow/<period_2>)\n"+
                    "where:\n"+
                    " - pow = max(1,redstone_power*<power_scaling>)\n"+
                    " - time = time in ticks\n"+
                    " - redstone_power = block redstone power\n"+
                    "<power_scaling> = how much frequency changes depending on power. 2 means it spins twice as fast each power level (2* for rain, 4* for thunder)\n" +
                    "increase to have more distinct indication when weather changes")
                    .defineInRange("power_scaling", 3.0, 1.0, 100.0);
            WIND_VANE_ANGLE_1 = builder.comment("Amplitude (maximum angle) of first sine wave")
                    .defineInRange("max_angle_1", 30.0, 0, 360);
            WIND_VANE_ANGLE_2 = builder.defineInRange("max_angle_2", 10.0, 0, 360);
            WIND_VANE_PERIOD_1 = builder.comment("Base period in ticks at 0 power of first sine wave")
                    .defineInRange("period_1", 450.0, 0.0, 2000.0);
            WIND_VANE_PERIOD_2 = builder.comment("This should be kept period_1/3 for a symmetric animation")
                    .defineInRange("period_2", 150.0, 0.0, 2000.0);
            builder.pop();

            builder.push("flag");
            FLAG_PERIOD = builder.comment("How slow a flag will oscillate. (Period of oscillation)\n"+
                    "Lower value = faster oscillation")
                    .defineInRange("slowness", 100, 0, 10000);
            FLAG_WAVELENGTH = builder.comment("How wavy the animation will be in pixels. (Wavelength)")
                    .defineInRange("wavyness", 6d, 0.001, 100);
            FLAG_AMPLITUDE = builder.comment("How tall the wave lobes will be. (Wave amplitude)")
                    .defineInRange("intensity", 1d, 0d, 100d);
            FLAG_AMPLITUDE_INCREMENT = builder.comment("How much the wave amplitude increases each pixel. (Amplitude increment per pixel)")
                    .defineInRange("intensity_increment", 0.3d, 0, 10);
            FLAG_FANCINESS = builder.comment("At which graphic settings flags will have a fancy renderer: 0=fast, 1=fancy, 2=fabulous")
                    .defineInRange("fanciness",2,0,2);
            builder.pop();
            //TODO: add more(hourGlass, sawying blocks...)


            builder.push("captured_mobs");

            CAPTURED_MOBS_PROPERTIES = builder.comment("Here you can customize how mobs are displayed in jars and cages.\n"+
                    "Following will have to be a list with the format below:\n"+
                    "[[<id>,<height>,<width>,<light_level>,<animation_type>],[<id>,...],...]\n"+
                    "With the following description:\n"+
                    " - <id> being the mob id (ie: minecraft:bee)\n"+
                    " - <height>,<width>: these are the added height and width that will be added to the actual mob hitbox to determine its scale inside a cage or jar \n"+
                    "   You can increase them so this 'adjusted hitbox' will match the actual mob shape\n"+
                    "   In other words increase the to make the mob smaller\n"+
                    " - <light_level> determines if and how much light should the mob emit\n"+
                    " - <animation_type> is used to associate each mob an animation.\n"+
                    "It can be set to the following values:\n" +
                    " - 'air' to make it stand in mid air like a flying animal (note that such mobs are set to this value by default)\n" +
                    " - 'land' to force it to stand on the ground even if it is a flying animal\n" +
                    " - 'floating' to to make it stand in mid air and wobble up and down\n" +
                    " - any number > 0 to make it render as a 2d fish whose index matches the 'fishies' texture sheet\n" +
                    " - 0 or any other values will be ignored and treated as default\n"+
                    "Note that only the first 3 parameters are needed, the others are optional")
                    .defineList("rendering_parameters", CapturedMobs.DEFAULT_CONFIG, s->true);
            builder.pop();

            builder.pop();
        }
    }


    public static class particle {
        public static ForgeConfigSpec.IntValue FIREFLY_PAR_MAXAGE;
        public static ForgeConfigSpec.DoubleValue FIREFLY_PAR_SCALE;
        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Particle parameters")
                    .push("particles");
            builder.comment("Firefly jar particle")
                    .push("firefly_glow");
            FIREFLY_PAR_SCALE = builder.comment("Scale")
                    .defineInRange("scale", 0.075, 0,1);
            FIREFLY_PAR_MAXAGE = builder.comment("Maximum age in ticks. Note that actual max age with be this + a random number between 0 and 10")
                    .defineInRange("max_age", 40, 1,256);
            builder.pop();

            builder.pop();
        }
    }

    public static class entity {
        public static ForgeConfigSpec.DoubleValue FIREFLY_INTENSITY;
        public static ForgeConfigSpec.DoubleValue FIREFLY_EXPONENT;
        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Entities parameters")
                    .push("entities");
            builder.push("firefly");
            FIREFLY_INTENSITY = builder.comment("Firefly glow animation uses following equation:\n"+
                    "scale = {max[(1-<intensity>)*sin(time*2pi/<period>)+<intensity>, 0]}^<exponent>\n"+
                    "Where:\n"+
                    " - scale = entity transparency & entity scale\n"+
                    " - period = period of the animation. This variable is located in common configs"+
                    "<intensity> affects how long the pulse last, not how frequently it occurs.\n"+
                    "Use 0.5 for normal sin wave. Higher and it won't turn off completely\n")
                    .defineInRange("intensity", 0.2,-100,1);
            FIREFLY_EXPONENT = builder.comment("Affects the shape of the wave. Stay under 0.5 for sharper transitions")
                    .defineInRange("exponent", 0.5, 0, 10);
            builder.pop();

            builder.pop();
        }
    }


    public static class cached {
        public static boolean COLORED_BWERING_STAND;
        public static boolean TOOLTIP_HINTS;
        public static int FIREFLY_PAR_MAXAGE;
        public static double FIREFLY_PAR_SCALE;
        public static double FIREFLY_INTENSITY;
        public static double FIREFLY_EXPONENT;
        public static double FIREFLY_SPAWN_CHANCE;
        public static int FIREFLY_SPAWN_PERIOD;
        public static boolean PEDESTAL_SPIN;
        public static double PEDESTAL_SPEED;
        public static boolean PEDESTAL_SPECIAL;
        public static boolean SHELF_TRANSLATE;
        public static double WIND_VANE_POWER_SCALING;
        public static double WIND_VANE_ANGLE_1;
        public static double WIND_VANE_ANGLE_2;
        public static double WIND_VANE_PERIOD_1;
        public static double WIND_VANE_PERIOD_2;
        public static boolean CLOCK_24H;
        public static boolean GLOBE_RANDOM;
        public static boolean CONFIG_BUTTON;
        public static int FLAG_PERIOD;
        public static double FLAG_WAVELENGTH;
        public static double FLAG_AMPLITUDE;
        public static double FLAG_AMPLITUDE_INCREMENT;
        public static int FLAG_FANCINESS;

        public static void refresh(){
            //tweaks
            COLORED_BWERING_STAND = tweaks.COLORED_BWERING_STAND.get();
            //general
            TOOLTIP_HINTS = general.TOOLTIP_HINTS.get();
            CONFIG_BUTTON = general.CONFIG_BUTTON.get();
            //particles
            FIREFLY_PAR_MAXAGE = particle.FIREFLY_PAR_MAXAGE.get();
            FIREFLY_PAR_SCALE = particle.FIREFLY_PAR_SCALE.get();
            //entities
            FIREFLY_INTENSITY = entity.FIREFLY_INTENSITY.get();
            FIREFLY_EXPONENT = entity.FIREFLY_EXPONENT.get();
            //blocks
            FIREFLY_SPAWN_CHANCE = block.FIREFLY_SPAWN_CHANCE.get();
            FIREFLY_SPAWN_PERIOD = block.FIREFLY_SPAWN_PERIOD.get();
            PEDESTAL_SPIN = block.PEDESTAL_SPIN.get();
            PEDESTAL_SPEED = block.PEDESTAL_SPEED.get();
            PEDESTAL_SPECIAL = block.PEDESTAL_SPECIAL.get();
            SHELF_TRANSLATE = block.SHELF_TRANSLATE.get();
            WIND_VANE_POWER_SCALING = block.WIND_VANE_POWER_SCALING.get();
            WIND_VANE_ANGLE_1 = block.WIND_VANE_ANGLE_1.get();
            WIND_VANE_ANGLE_2 = block.WIND_VANE_ANGLE_2.get();
            WIND_VANE_PERIOD_1 = block.WIND_VANE_PERIOD_1.get();
            WIND_VANE_PERIOD_2 = block.WIND_VANE_PERIOD_2.get();
            CLOCK_24H = block.CLOCK_24H.get();
            GLOBE_RANDOM = block.GLOBE_RANDOM.get();
            FLAG_AMPLITUDE = block.FLAG_AMPLITUDE.get();
            FLAG_AMPLITUDE_INCREMENT = block.FLAG_AMPLITUDE_INCREMENT.get();
            FLAG_PERIOD = block.FLAG_PERIOD.get();
            FLAG_WAVELENGTH = block.FLAG_WAVELENGTH.get();
            FLAG_FANCINESS = block.FLAG_FANCINESS.get();

            CapturedMobs.refresh();
            GlobeTextureManager.GlobeColors.refreshColorsFromConfig();

        }
    }



}
