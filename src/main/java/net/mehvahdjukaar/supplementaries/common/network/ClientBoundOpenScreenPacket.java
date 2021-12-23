package net.mehvahdjukaar.supplementaries.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundOpenScreenPacket implements NetworkHandler.Message {
    private final BlockPos pos;

    public ClientBoundOpenScreenPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public ClientBoundOpenScreenPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void buffer(ClientBoundOpenScreenPacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
    }

    public static void handler(ClientBoundOpenScreenPacket message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleOpenScreenPacket(message);
            }
        });
        context.setPacketHandled(true);
    }

    public BlockPos getPos() {
        return pos;
    }
}