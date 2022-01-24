package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundSpawnBlockParticlePacket implements NetworkHandler.Message {
    public final BlockPos pos;
    public final ParticleUtil.EventType id;

    public ClientBoundSpawnBlockParticlePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.id = buffer.readEnum(ParticleUtil.EventType.class);
    }

    public ClientBoundSpawnBlockParticlePacket(BlockPos pos, ParticleUtil.EventType id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(ClientBoundSpawnBlockParticlePacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeEnum(message.id);
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

}