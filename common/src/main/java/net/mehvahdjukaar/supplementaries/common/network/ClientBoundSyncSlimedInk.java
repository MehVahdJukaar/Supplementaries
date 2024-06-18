package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;


public record ClientBoundSyncSlimedInk(int id, int duration) implements Message {

    public ClientBoundSyncSlimedInk(FriendlyByteBuf buffer) {
          this(buffer.readVarInt(), buffer.readVarInt());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeVarInt(this.duration);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleSyncSlimed(this);
    }
}