package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record ClientBoundSyncGlobeDataPacket(GlobeData data) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncGlobeDataPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_globe_data"), ClientBoundSyncGlobeDataPacket::new);

    public ClientBoundSyncGlobeDataPacket(RegistryFriendlyByteBuf buffer) {
        this(new GlobeData(buffer.readNbt()));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(this.data.save(new CompoundTag()));
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