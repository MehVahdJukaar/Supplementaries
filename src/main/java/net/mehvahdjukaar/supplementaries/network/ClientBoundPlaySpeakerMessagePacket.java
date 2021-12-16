package net.mehvahdjukaar.supplementaries.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundPlaySpeakerMessagePacket implements NetworkHandler.Message {
    private final Component str;
    private final boolean narrator;

    public ClientBoundPlaySpeakerMessagePacket(FriendlyByteBuf buf) {
        this.str = buf.readComponent();
        this.narrator = buf.readBoolean();
    }

    public ClientBoundPlaySpeakerMessagePacket(Component str, boolean narrator) {
        this.str = str;
        this.narrator = narrator;
    }

    public static void buffer(ClientBoundPlaySpeakerMessagePacket message, FriendlyByteBuf buf) {
        buf.writeComponent(message.str);
        buf.writeBoolean(message.narrator);
    }

    public static void handler(ClientBoundPlaySpeakerMessagePacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handlePlaySpeakerMessagePacket(message);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public boolean getNarrator() {
        return narrator;
    }

    public Component getStr() {
        return str;
    }
}