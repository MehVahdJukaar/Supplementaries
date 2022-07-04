package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;
import java.util.UUID;

//does some on login stuff
public class ClientBoundSendLoginPacket implements Message {

    public final Map<UUID, String> usernameCache;

    public ClientBoundSendLoginPacket(FriendlyByteBuf buf) {
        this.usernameCache = buf.readMap(FriendlyByteBuf::readUUID, FriendlyByteBuf::readUtf);
    }

    public ClientBoundSendLoginPacket(Map<UUID, String> usernameCache) {
        this.usernameCache = usernameCache;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeMap(this.usernameCache, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeUtf);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleLoginPacket(this);
    }
}