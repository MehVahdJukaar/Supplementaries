package net.mehvahdjukaar.supplementaries.configs;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigs {
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.BooleanValue TEST;
    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        animationsConfigs(CLIENT_BUILDER);

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    private static void animationsConfigs(ForgeConfigSpec.Builder builder){
        builder.comment("Tweak and change the various block animations");
        builder.comment("if you find a configuration that looks better send it to me and I'll add it as a new default");
        builder.push("animations");
        TEST = builder.define("test",true);
        builder.pop();
    }
}
