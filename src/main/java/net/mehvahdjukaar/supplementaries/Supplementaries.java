package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.datagen.RecipeCondition;
import net.mehvahdjukaar.supplementaries.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.setup.ClientSetup;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSetup;
import net.mehvahdjukaar.supplementaries.world.structures.StructureRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Supplementaries.MOD_ID)
public class Supplementaries{

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String n){
        return new ResourceLocation(MOD_ID,n);
    }
    public static String str(String n){
        return MOD_ID+":"+n;
    }

    public Supplementaries() {

        //yes this is where I write crap

        //add alliance contract item

        // zipline mod ropewalk

        //GLOBE inv model
        //TODO: goblet & jars dynamic baked model
        //ghast fireball mob griefing

        //Bamboo spikes damage fall

        //TODO: fireflies deflect arrows

        //TODO: fix tint breaking particle like grass block

        //TODO: replace soft fluid system with forge caps to itemstacks and register actual forge fluids

        //TODO: reworkd ItemDisplayTile with proper capability usage

        //TODO: bugs: bell ropes(add to flywheel instance), brewing starnd colors(?)

        //TODO: notice board pages, rewrite inventory cap

        //TODO: mod ideas: particle block, blackboard banners and flags, lantern holding

        //TODO: add stick window loggable clipping

        //TODO: add redstone config for iron gate

        //TODO: fix horizontal stick texture

        //mod idea: better birch trees

        //mod idea: blackboard banners and flags with villager

        //slingshot that places blocks

        MinecraftForge.EVENT_BUS.register(ServerEvents.class);


        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ServerConfigs.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.CLIENT_SPEC);

        ConfigHandler.init();


        CraftingHelper.register(new RecipeCondition.Serializer(RecipeCondition.MY_FLAG));

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegistry.init(bus);

        StructureRegistry.init(bus);

        bus.addListener(ModSetup::init);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientSetup::init));




    }


}
