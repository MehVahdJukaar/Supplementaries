package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.moonlight.configs.ConfigHelper;
import net.mehvahdjukaar.moonlight.configs.SyncedCommonConfigs;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.RequestConfigReloadPacket;
import net.mehvahdjukaar.supplementaries.common.network.SyncConfigsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {

    public static ModConfig CLIENT_CONFIG_OBJECT;
    public static ModConfig SERVER_CONFIG_OBJECT;
    public static ModConfig REGISTRY_CONFIG_OBJECT;

    public static void registerBus(IEventBus modBus) {
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();

        SERVER_CONFIG_OBJECT = new SyncedCommonConfigs(ServerConfigs.SERVER_SPEC,modContainer){
            @Override
            public void onRefresh() {
                ServerConfigs.cached.refresh();
            }
        };
        CLIENT_CONFIG_OBJECT = new ModConfig(ModConfig.Type.CLIENT, ClientConfigs.CLIENT_SPEC, modContainer);
        //SERVER_CONFIG_OBJECT = new ModConfig(ModConfig.Type.COMMON, ServerConfigs.SERVER_SPEC, modContainer);
        REGISTRY_CONFIG_OBJECT = new ModConfig(ModConfig.Type.COMMON, RegistryConfigs.REGISTRY_CONFIG, modContainer, RegistryConfigs.FILE_NAME);
        modContainer.addConfig(CLIENT_CONFIG_OBJECT);
        modContainer.addConfig(SERVER_CONFIG_OBJECT);

        //need to register on 2 different busses, can't use subscribe event
        MinecraftForge.EVENT_BUS.addListener(ConfigHandler::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(ConfigHandler::onPlayerLoggedOut);
        modBus.addListener(ConfigHandler::reloadConfigsEvent);
    }

    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();

        mc.setScreen(ModList.get().getModContainerById(Supplementaries.MOD_ID).get()
                .getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class).get().screenFunction()
                .apply(mc, mc.screen));
    }

    public static void reloadConfigsEvent(ModConfigEvent event) {
        /*
        if (event.getConfig().getSpec() == ServerConfigs.SERVER_SPEC) {
            //send this configuration to connected clients
            sendSyncedConfigsToAllPlayers();
            ServerConfigs.cached.refreshVisuals();
        } else if (event.getConfig().getSpec() == ClientConfigs.CLIENT_SPEC)
            ClientConfigs.cached.refreshVisuals();
        */
    }


    @Deprecated
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        /*
        if (!event.getPlayer().level.isClientSide) {
            //send this configuration to connected clients
            syncServerConfigs((ServerPlayer) event.getPlayer());
        }*/
    }
    @Deprecated
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        /*
        if (event.getPlayer().level.isClientSide) {
            //reload local common configs
            //maybe not needed
            //ServerConfigs.loadLocal();
            ServerConfigs.cached.refreshVisuals();
        }*/
    }


    public static Path getServerConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(Supplementaries.MOD_ID + "-common.toml").toAbsolutePath();
    }


    //called on client. client -> server -..-> all clients
    public static void clientRequestServerConfigReload() {
        if(Minecraft.getInstance().getConnection()!=null)
        NetworkHandler.INSTANCE.sendToServer(new RequestConfigReloadPacket());
    }

    //called on server. sync server -> all clients
    public static void sendSyncedConfigsToAllPlayers() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            PlayerList playerList = currentServer.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                syncServerConfigs(player);
            }
        }
    }

    //send configs from server -> client
    public static void syncServerConfigs(ServerPlayer player) {
        try {
            final byte[] configData = Files.readAllBytes(getServerConfigPath());
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncConfigsPacket(configData));
        } catch (IOException e) {
            Supplementaries.LOGGER.error(Supplementaries.MOD_ID + ": Failed to sync common configs", e);
        }
    }

}
