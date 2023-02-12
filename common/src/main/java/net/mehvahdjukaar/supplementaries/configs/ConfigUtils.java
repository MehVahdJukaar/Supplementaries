package net.mehvahdjukaar.supplementaries.configs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.RequestConfigReloadPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class ConfigUtils {


    @ExpectPlatform
    public static void openModConfigs() {
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
        CommonConfigs.SPEC.loadFromFile();
        CommonConfigs.SPEC.syncConfigsToPlayer(player);
    }

}
