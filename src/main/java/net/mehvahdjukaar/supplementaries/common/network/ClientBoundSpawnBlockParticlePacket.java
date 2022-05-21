package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundSpawnBlockParticlePacket implements NetworkHandler.Message {
    public final Vec3 pos;
    public final ParticleUtil.EventType id;

    public ClientBoundSpawnBlockParticlePacket(FriendlyByteBuf buffer) {
        this.pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.id = buffer.readEnum(ParticleUtil.EventType.class);
    }

    public ClientBoundSpawnBlockParticlePacket(BlockPos pos, ParticleUtil.EventType id) {
        this(Vec3.atCenterOf(pos),id);
    }

    public ClientBoundSpawnBlockParticlePacket(Vec3 pos, ParticleUtil.EventType id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(ClientBoundSpawnBlockParticlePacket message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.pos.x);
        buffer.writeDouble(message.pos.y);
        buffer.writeDouble(message.pos.z);
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