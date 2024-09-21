package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimeData;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientBoundSyncHourglassPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncHourglassPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_hourglass"),
            ClientBoundSyncHourglassPacket::new);

    protected final List<HourglassTimeData> hourglass;

    public ClientBoundSyncHourglassPacket(final Collection<HourglassTimeData> data) {
        this.hourglass = List.copyOf(data);
    }

    public ClientBoundSyncHourglassPacket(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        this.hourglass = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.hourglass.add(HourglassTimeData.STREAM_CODEC.decode(buf));
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.hourglass.size());
        for (var entry : this.hourglass) {
            HourglassTimeData.STREAM_CODEC.encode(buf, entry);
        }
    }

    @Override
    public void handle(Context context) {
        //client world
        HourglassTimesManager.INSTANCE.setData(this.hourglass);
        Supplementaries.LOGGER.info("Synced Hourglass data");
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
