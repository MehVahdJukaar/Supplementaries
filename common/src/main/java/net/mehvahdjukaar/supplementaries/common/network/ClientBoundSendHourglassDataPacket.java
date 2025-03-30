package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimeData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientBoundSendHourglassDataPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendHourglassDataPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_hourglass"),
            ClientBoundSendHourglassDataPacket::new);

    protected final List<HourglassTimeData> hourglassTimes;

    public ClientBoundSendHourglassDataPacket(final Collection<HourglassTimeData> data) {
        this.hourglassTimes = List.copyOf(data);
    }

    public ClientBoundSendHourglassDataPacket(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        this.hourglassTimes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.hourglassTimes.add(HourglassTimeData.STREAM_CODEC.decode(buf));
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.hourglassTimes.size());
        for (var entry : this.hourglassTimes) {
            HourglassTimeData.STREAM_CODEC.encode(buf, entry);
        }
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncHourglassData(this);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
