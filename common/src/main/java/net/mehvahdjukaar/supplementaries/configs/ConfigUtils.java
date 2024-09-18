package net.mehvahdjukaar.supplementaries.configs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestConfigReloadPacket;
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
            NetworkHelper.sendToServer(new ServerBoundRequestConfigReloadPacket());
    }


    //called from config screen
    public static void configScreenReload(ServerPlayer player) {
        //TODO: fix configs sinking and remove this. idk why its needed
        CommonConfigs.CONFIG_HOLDER.loadFromFile();
        CommonConfigs.CONFIG_HOLDER.syncConfigsToPlayer(player);
    }

}
