package net.mehvahdjukaar.supplementaries.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundSpawnBlockParticlePacket implements NetworkHandler.Message {
    private final BlockPos pos;
    private final int id;

    public ClientBoundSpawnBlockParticlePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.id = buffer.readInt();
    }

    public ClientBoundSpawnBlockParticlePacket(BlockPos pos, int id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(ClientBoundSpawnBlockParticlePacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeInt(message.id);
    }

    public static void handler(ClientBoundSpawnBlockParticlePacket message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSpawnBlockParticlePacket(message);
            }
        });

        context.setPacketHandled(true);
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getId() {
        return id;
    }
}