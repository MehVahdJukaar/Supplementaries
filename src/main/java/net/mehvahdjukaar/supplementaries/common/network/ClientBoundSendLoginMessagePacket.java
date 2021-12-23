package net.mehvahdjukaar.supplementaries.common.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSendLoginMessagePacket {
    public ClientBoundSendLoginMessagePacket(FriendlyByteBuf buf) {
    }


    public ClientBoundSendLoginMessagePacket() {
    }

    public static void buffer(ClientBoundSendLoginMessagePacket message, FriendlyByteBuf buf) {
    }

    public static void handler(ClientBoundSendLoginMessagePacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSendLoginMessagePacket(message);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}