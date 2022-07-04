package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class RequestConfigReloadPacket implements Message {
    public RequestConfigReloadPacket(FriendlyByteBuf buffer) {
    }

    public RequestConfigReloadPacket() {
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ConfigHandler.configScreenReload((ServerPlayer) context.getSender());
    }

}