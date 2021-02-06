package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SyncConfigsPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {

    public static void init(){
        //need to register on 2 different busses, can't use subscribe event
        MinecraftForge.EVENT_BUS.addListener(ConfigHandler::onPlayerLoggedIn);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigHandler::reloadConfigsEvent);
    }


    public static void reloadConfigsEvent(ModConfig.ModConfigEvent event) {
        if(event.getConfig().getSpec() == ServerConfigs.SERVER_CONFIG) {
            //send this configuration to connected clients
            syncServerConfigs();
            ServerConfigs.cached.refresh();
        }
        else if(event.getConfig().getSpec() == ClientConfigs.CLIENT_CONFIG)
            ClientConfigs.cached.refresh();
    }


    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote) {
            syncServerConfigs(event.getPlayer());
        }
    }



    public static Path getServerConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(Supplementaries.MOD_ID + "-common.toml").toAbsolutePath();
    }

    public static void syncServerConfigs() {
        final MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            final PlayerList playerList = currentServer.getPlayerList();
            if (playerList != null) {
                for (ServerPlayerEntity player : playerList.getPlayers()) {
                    syncServerConfigs(player);
                }
            }
        }

    }

    public static void syncServerConfigs(PlayerEntity player) {
        try {
            final byte[] configData = Files.readAllBytes(getServerConfigPath());
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SyncConfigsPacket(configData));
        } catch (IOException e) {
            Supplementaries.LOGGER.error(Supplementaries.MOD_ID+ ": Failed to sync common configs", e);
        }
    }

}
