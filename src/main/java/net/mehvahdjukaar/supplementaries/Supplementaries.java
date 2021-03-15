package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.datagen.RecipeCondition;
import net.mehvahdjukaar.supplementaries.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.setup.ClientSetup;
import net.mehvahdjukaar.supplementaries.setup.ModSetup;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.CampfireBlock;
import net.minecraft.item.HoeItem;
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

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.CodeSource;

@Mod(Supplementaries.MOD_ID)
public class Supplementaries{

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger();

    public Supplementaries() {


        MinecraftForge.EVENT_BUS.register(ServerEvents.class);

        try {
            RegistryConfigs.load();
        }catch (Exception exception){
            throw new RuntimeException("Failed to load config supplementaries-registry.toml. Try deleting it");
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ServerConfigs.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.CLIENT_CONFIG);

        ConfigHandler.init();

        CraftingHelper.register(new RecipeCondition.Serializer(RecipeCondition.MY_FLAG));

        NetworkHandler.registerMessages();

        //TODO: make flax like hay

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Registry.init(bus);

        bus.addListener(ModSetup::init);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientSetup::init));




    }


}
