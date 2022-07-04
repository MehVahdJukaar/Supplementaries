package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.moonlight.configs.ConfigHelper;
import net.mehvahdjukaar.moonlight.configs.SyncedModConfigs;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.RequestConfigReloadPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ConfigHandler {

    public static ModConfig CLIENT_CONFIGS;
    public static SyncedModConfigs SERVER_CONFIGS;
    public static ModConfig REGISTRY_CONFIGS;

    public static void registerBus(IEventBus modBus) {
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();

        SERVER_CONFIGS = new SyncedModConfigs(ServerConfigs.SERVER_SPEC, modContainer) {
            @Override
            public void onRefresh() {
                ServerConfigs.cached.refresh();
            }
        };
        CLIENT_CONFIGS = new ModConfig(ModConfig.Type.CLIENT, ClientConfigs.CLIENT_SPEC, modContainer);
        //SERVER_CONFIG_OBJECT = new ModConfig(ModConfig.Type.COMMON, ServerConfigs.SERVER_SPEC, modContainer);
        REGISTRY_CONFIGS = new ModConfig(ModConfig.Type.COMMON, RegistryConfigs.REGISTRY_CONFIG, modContainer, RegistryConfigs.FILE_NAME);
        modContainer.addConfig(CLIENT_CONFIGS);
        modContainer.addConfig(SERVER_CONFIGS);
        // modContainer.addConfig(REGISTRY_CONFIGS); //??

        modBus.addListener(ConfigHandler::reloadConfigsEvent);
    }

    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();

        mc.setScreen(ModList.get().getModContainerById(Supplementaries.MOD_ID).get()
                .getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class).get().screenFunction()
                .apply(mc, mc.screen));
    }

    public static void reloadConfigsEvent(ModConfigEvent event) {

        if (event.getConfig().getSpec() == ServerConfigs.SERVER_SPEC) {
            //send this configuration to connected clients

            // sendSyncedConfigsToAllPlayers();
            // ServerConfigs.cached.refresh();
        } else if (event.getConfig().getSpec() == ClientConfigs.CLIENT_SPEC)
            ClientConfigs.cached.refresh();

    }

    //TODO: check this

    //called on client. client -> server -..-> all clients
    public static void clientRequestServerConfigReload() {
        if (Minecraft.getInstance().getConnection() != null)
            NetworkHandler.INSTANCE.sendToServer(new RequestConfigReloadPacket());
    }

    //called on server. sync server -> all clients
    public static void sendSyncedConfigsToAllPlayers() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            PlayerList playerList = currentServer.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                SERVER_CONFIGS.syncConfigs(player);
            }
        }
    }

    //called from config screen
    public static void configScreenReload(ServerPlayer player) {
        //TODO: fix configs sinking and remove this. idk why its needed
        ConfigHelper.reloadConfigFile(SERVER_CONFIGS);
        SERVER_CONFIGS.syncConfigs(player);
        ServerConfigs.cached.refresh();
    }

}
