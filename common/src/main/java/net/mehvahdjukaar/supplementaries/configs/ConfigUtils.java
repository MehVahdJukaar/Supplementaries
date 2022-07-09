package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.RequestConfigReloadPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class ConfigUtils {


    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();

        mc.setScreen(ServerConfigs.SERVER_SPEC.makeScreen(mc.screen));
    }


    //TODO: check this

    //called on client. client -> server -..-> all clients
    public static void clientRequestServerConfigReload() {
        if (Minecraft.getInstance().getConnection() != null)
            NetworkHandler.CHANNEL.sendToServer(new RequestConfigReloadPacket());
    }


    //called from config screen
    public static void configScreenReload(ServerPlayer player) {
        //TODO: fix configs sinking and remove this. idk why its needed
        ServerConfigs.SERVER_SPEC.loadFromFile();
        ServerConfigs.SERVER_SPEC.syncConfigsToPlayer(player);
    }

}
