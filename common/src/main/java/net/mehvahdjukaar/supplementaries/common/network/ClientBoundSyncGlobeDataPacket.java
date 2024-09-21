package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public class ClientBoundSyncGlobeDataPacket implements Message {
    private final GlobeData data;

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncGlobeDataPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_globe_data"), ClientBoundSyncGlobeDataPacket::new);

    public ClientBoundSyncGlobeDataPacket(GlobeData data) {
        this.data = data;
    }

    public ClientBoundSyncGlobeDataPacket(RegistryFriendlyByteBuf buffer) {
        this.data = GlobeData.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        GlobeData.STREAM_CODEC.encode(buf, this.data);
    }

    @Override
    public void handle(Context context) {
        //assigns data to client
        GlobeData.setClientData(this.data);
        Supplementaries.LOGGER.info("Synced Globe data");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}