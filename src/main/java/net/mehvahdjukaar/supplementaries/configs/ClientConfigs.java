package net.mehvahdjukaar.supplementaries.configs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class ClientConfigs {
    public static ForgeConfigSpec CLIENT_CONFIG;

    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        block.init(CLIENT_BUILDER);
        particle.init(CLIENT_BUILDER);
        entity.init(CLIENT_BUILDER);

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static class block {
        public static ForgeConfigSpec.DoubleValue FIREFLY_SPAWN_CHANCE;
        public static ForgeConfigSpec.IntValue FIREFLY_SPAWN_PERIOD;
        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("Tweak and change the various block animations +\n" +
                    "IF YOU FIND A CONFIG THAT LOOKS BETTER THAT THESE DEFAULTS, SEND IT TO ME AND I'LL MAKE IT THE NEW DEFAUL\n"+
                    "lots configs more coming next updates")
                    .push("blocks");

            builder.push("firefly_jar");
            FIREFLY_SPAWN_PERIOD = builder.comment("particle spawn if this equation is true: time%period==0 and randomfloat>chance  where random float is a random number between 0.0 and 1.0\n"+
                    "how often they try to spawn")
                    .defineInRange("spawn_period",8, 1, 20);
            FIREFLY_SPAWN_CHANCE = builder.comment("spawn chance every period")
                    .defineInRange("spawn_chance", 0.6, 0, 1);
            builder.pop();

            builder.pop();
        }
    }



    public static class particle {
        public static ForgeConfigSpec.IntValue FIREFLY_PAR_MAXAGE;
        public static ForgeConfigSpec.DoubleValue FIREFLY_PAR_SCALE;
        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("particle parameters")
                    .push("particles");
            builder.comment("firefly jar particle")
                    .push("firefly_glow");
            FIREFLY_PAR_SCALE = builder.comment("scale multiplier")
                    .defineInRange("scale", 0.075, 0,1);
            FIREFLY_PAR_MAXAGE = builder.comment("max age. Note that actual max age with be this + a random number between 0 and 10")
                    .defineInRange("max_age", 40, 1,256);
            builder.pop();

            builder.pop();
        }
    }

    public static class entity {
        public static ForgeConfigSpec.IntValue FIREFLY_PERIOD;
        public static ForgeConfigSpec.DoubleValue FIREFLY_SCALE;
        public static ForgeConfigSpec.DoubleValue FIREFLY_INTENSITY;
        public static ForgeConfigSpec.DoubleValue FIREFLY_EXPONENT;
        private static void init(ForgeConfigSpec.Builder builder) {
            builder.comment("entities parameters")
                    .push("entities");
            builder.push("firefly");
            FIREFLY_SCALE = builder.comment("scale multiplier")
                    .defineInRange("scale", 0.15, 0,1);
            FIREFLY_PERIOD = builder.comment("glow animation uses following euation:\n"+
                    "alpha = scale = {max[(1-intensity)*sin(time*2pi/period)+intensity, 0]}^exponent\n"+
                    "note that actual period will be this + a random number between 0 and 10")
                    .defineInRange("period", 65, 1,200);
            FIREFLY_INTENSITY = builder.comment("affects how long the pulse last, not how frequently it occurs. 0.5 for normal sin wave. higher and it won't turn off completely")
                    .defineInRange("intensity", 0.2,-100,1);
            FIREFLY_EXPONENT = builder.comment("affects the shape of the wave. stay under 0.5 for sharper transitions")
                    .defineInRange("exponent", 0.5, 0, 10);
            builder.pop();

            builder.pop();
        }
    }


    public static class cached {
        public static int FIREFLY_PAR_MAXAGE;
        public static double FIREFLY_PAR_SCALE;
        public static int FIREFLY_PERIOD;
        public static double FIREFLY_SCALE;
        public static double FIREFLY_INTENSITY;
        public static double FIREFLY_EXPONENT;
        public static double FIREFLY_SPAWN_CHANCE;
        public static int FIREFLY_SPAWN_PERIOD;

        public static void refresh(){
            //particles
            FIREFLY_PAR_MAXAGE = particle.FIREFLY_PAR_MAXAGE.get();
            FIREFLY_PAR_SCALE = particle.FIREFLY_PAR_SCALE.get();
            //entities
            FIREFLY_PERIOD = entity.FIREFLY_PERIOD.get();
            FIREFLY_SCALE = entity.FIREFLY_SCALE.get();
            FIREFLY_INTENSITY = entity.FIREFLY_INTENSITY.get();
            FIREFLY_EXPONENT = entity.FIREFLY_EXPONENT.get();
            //blocks
            FIREFLY_SPAWN_CHANCE = block.FIREFLY_SPAWN_CHANCE.get();
            FIREFLY_SPAWN_PERIOD = block.FIREFLY_SPAWN_PERIOD.get();


        }
    }



}
