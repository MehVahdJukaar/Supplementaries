package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;


public class ClientBoundOpenScreenPacket implements Message {
    public final BlockPos pos;

    public ClientBoundOpenScreenPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public ClientBoundOpenScreenPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleOpenScreenPacket(this);
    }

}