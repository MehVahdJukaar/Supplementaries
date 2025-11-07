package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientBoundFinalizeBookDataPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundFinalizeBookDataPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_books"),
            ClientBoundFinalizeBookDataPacket::new);

    public ClientBoundFinalizeBookDataPacket() {
    }

    public ClientBoundFinalizeBookDataPacket(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleFinalizeBookData(this);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
