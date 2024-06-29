package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record ClientBoundControlCannonPacket(BlockPos pos) implements Message {


    public ClientBoundControlCannonPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
          buf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // client world
        ClientReceivers.handleCannonControlPacket(this);
    }
}