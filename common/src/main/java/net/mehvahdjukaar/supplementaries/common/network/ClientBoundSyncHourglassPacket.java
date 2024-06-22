package net.mehvahdjukaar.supplementaries.common.network;

import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimeData;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientBoundSyncHourglassPacket implements Message {

    protected final List<HourglassTimeData> hourglass;

    public ClientBoundSyncHourglassPacket(final Collection<HourglassTimeData> songs) {
        this.hourglass = List.copyOf(songs);
    }

    public ClientBoundSyncHourglassPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.hourglass = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag != null) {
                DataResult<HourglassTimeData> r = HourglassTimeData.NETWORK_CODEC.parse(NbtOps.INSTANCE, tag);
                r.result().ifPresent(hourglass::add);
            }
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(this.hourglass.size());
        for (var entry : this.hourglass) {
            DataResult<Tag> r = HourglassTimeData.NETWORK_CODEC.encodeStart(NbtOps.INSTANCE, entry);
            if (r.result().isPresent()) {
                buf.writeNbt((CompoundTag) r.result().get());
            }
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //client world
        HourglassTimesManager.INSTANCE.setData(this.hourglass);
        Supplementaries.LOGGER.info("Synced Hourglass data");
    }


}
