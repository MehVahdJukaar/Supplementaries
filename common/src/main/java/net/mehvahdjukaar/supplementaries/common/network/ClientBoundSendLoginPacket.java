package net.mehvahdjukaar.supplementaries.common.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

//does some on login stuff
public class ClientBoundSendLoginPacket {

    public final Map<UUID, String> usernameCache;

    public ClientBoundSendLoginPacket(FriendlyByteBuf buf) {
        this.usernameCache = buf.readMap(FriendlyByteBuf::readUUID, FriendlyByteBuf::readUtf);
    }


    public ClientBoundSendLoginPacket(Map<UUID, String> usernameCache) {
        this.usernameCache = usernameCache;
    }

    public static void buffer(ClientBoundSendLoginPacket message, FriendlyByteBuf buf) {
        buf.writeMap(message.usernameCache, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeUtf);
    }

    public static void handler(ClientBoundSendLoginPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleLoginPacket(message);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}