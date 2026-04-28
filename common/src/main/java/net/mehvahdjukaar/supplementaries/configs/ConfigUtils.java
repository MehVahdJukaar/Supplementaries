package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestConfigReloadPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class ConfigUtils {


    @PlatformImpl
    public static void openModConfigs() {
        throw new AssertionError();
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
        CommonConfigs.CONFIG_HOLDER.forceLoad();
        CommonConfigs.CONFIG_HOLDER.syncConfigsToPlayer(player);
    }

}
