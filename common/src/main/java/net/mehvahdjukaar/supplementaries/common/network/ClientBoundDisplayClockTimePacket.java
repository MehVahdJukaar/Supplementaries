package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;

public class ClientBoundDisplayClockTimePacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundDisplayClockTimePacket> CODEC = Message.makeType(
            Supplementaries.res("show_time"),
            ClientBoundDisplayClockTimePacket::new);

    public final Optional<Long> time;

    public ClientBoundDisplayClockTimePacket(long time, boolean natural) {
        this.time = natural ? Optional.empty() : Optional.of(time);
    }

    public ClientBoundDisplayClockTimePacket(RegistryFriendlyByteBuf buf) {
        this.time = buf.readOptional(object -> buf.readVarLong());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeOptional(time, FriendlyByteBuf::writeVarLong);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.showTime(this);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
