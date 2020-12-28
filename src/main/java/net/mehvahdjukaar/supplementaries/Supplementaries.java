package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.setup.ClientSetup;
import net.mehvahdjukaar.supplementaries.setup.ModSetup;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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


    public Supplementaries() {

        RegistryConfigs.registerConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ServerConfigs.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.CLIENT_CONFIG);


        Registry.init();

        Networking.registerMessages();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ModSetup::init);
        bus.addListener(ClientSetup::init);

        bus.addListener(ServerConfigs::configEvent);

        MinecraftForge.EVENT_BUS.register(GlobeData.class);


    }


}
