package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public record ClientBoundSyncGlobeDataPacket(GlobeData data) implements Message {

    public ClientBoundSyncGlobeDataPacket(FriendlyByteBuf buffer) {
        this(new GlobeData(buffer.readNbt()));
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeNbt(this.data.save(new CompoundTag()));
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //assigns data to client
        GlobeData.setClientData(this.data);
        Supplementaries.LOGGER.info("Synced Globe data");
    }
}