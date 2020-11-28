package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.events.RightClickEvent;
import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.network.SendSpeakerBlockMessagePacket;
import net.mehvahdjukaar.supplementaries.setup.ClientSetup;
import net.mehvahdjukaar.supplementaries.setup.Dispenser;
import net.mehvahdjukaar.supplementaries.setup.ModSetup;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Supplementaries.MOD_ID)
public class Supplementaries{

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger();

    public Supplementaries() {

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ServerConfigs.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.CLIENT_CONFIG);

        Registry.init();

        Networking.registerMessages();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ModSetup::init);
        bus.addListener(ClientSetup::init);

        bus.addListener(ServerConfigs::configEvent);
        //bus.addListener(ServerConfigs::reloadConfig);

    }


}
