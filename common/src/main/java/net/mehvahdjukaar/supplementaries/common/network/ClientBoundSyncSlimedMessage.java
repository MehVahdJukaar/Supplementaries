package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record ClientBoundSyncSlimedMessage(int id, int duration) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncSlimedMessage> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_slimed"), ClientBoundSyncSlimedMessage::new);

    public ClientBoundSyncSlimedMessage(FriendlyByteBuf buffer) {
          this(buffer.readVarInt(), buffer.readVarInt());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeVarInt(this.duration);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncSlimed(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}