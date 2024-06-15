package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;


public record ClientBoundSyncAntiqueInk(BlockPos pos, boolean ink) implements Message {

    public ClientBoundSyncAntiqueInk(FriendlyByteBuf buffer) {
          this(buffer.readBlockPos(), buffer.readBoolean());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.ink);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleSyncAntiqueInkPacket(this);
    }
}