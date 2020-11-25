package net.mehvahdjukaar.supplementaries.configs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class ClientConfigs {
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.BooleanValue TEST;
    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        animationsConfigs(CLIENT_BUILDER);

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    private static void animationsConfigs(ForgeConfigSpec.Builder builder){
        builder.comment("Tweak and change the various block animations +\n"+
                "if you find a configuration that looks better send it to me and I'll add it as a new default")
                .push("animations");
        TEST = builder.comment("lots of animation settings coming next update(hopefully)")
                .define("test",true);



        builder.pop();
    }


    public static class cached {
        //animations


        public static void refresh(){

        }
    }



    @SubscribeEvent
    public static void loadConfig(final ModConfig.Loading event) {
        if(event.getConfig().getType() == ModConfig.Type.CLIENT)
            cached.refresh();
    }

    @SubscribeEvent
    public static void reloadConfig(final ModConfig.Reloading event) {
        int a = 1;
        if(event.getConfig().getType() == ModConfig.Type.CLIENT)
            cached.refresh();
    }

}
