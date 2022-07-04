package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;


public class ClientBoundSyncAntiqueInk implements Message {
    public final BlockPos pos;
    public final boolean ink;

    public ClientBoundSyncAntiqueInk(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.ink = buffer.readBoolean();
    }

    public ClientBoundSyncAntiqueInk(BlockPos pos, boolean ink) {
        this.pos = pos;
        this.ink = ink;
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