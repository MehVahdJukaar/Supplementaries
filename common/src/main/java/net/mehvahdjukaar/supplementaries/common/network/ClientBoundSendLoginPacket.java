package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;
import java.util.UUID;

//does some on login stuff
public record ClientBoundSendLoginPacket(Map<UUID, String> usernameCache) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendLoginPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_send_login"), ClientBoundSendLoginPacket::new);

    public ClientBoundSendLoginPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readMap(RegistryFriendlyByteBuf::readUUID, FriendlyByteBuf::readUtf));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeMap(this.usernameCache, RegistryFriendlyByteBuf::writeUUID, FriendlyByteBuf::writeUtf);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleLoginPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}