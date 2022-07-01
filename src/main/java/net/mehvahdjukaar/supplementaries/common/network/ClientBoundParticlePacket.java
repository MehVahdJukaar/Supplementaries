package net.mehvahdjukaar.supplementaries.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundParticlePacket implements NetworkHandler.Message {
    public final Vec3 pos;
    public final EventType id;

    public ClientBoundParticlePacket(FriendlyByteBuf buffer) {
        this.pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.id = buffer.readEnum(EventType.class);
    }

    public ClientBoundParticlePacket(BlockPos pos, EventType id) {
        this(Vec3.atCenterOf(pos), id);
    }

    public ClientBoundParticlePacket(Vec3 pos, EventType id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(ClientBoundParticlePacket message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.pos.x);
        buffer.writeDouble(message.pos.y);
        buffer.writeDouble(message.pos.z);
        buffer.writeEnum(message.id);
    }

    public static void handler(ClientBoundParticlePacket message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSpawnBlockParticlePacket(message);
            }
        });

        context.setPacketHandled(true);
    }

    public enum EventType {
        BUBBLE_BLOW,
        BUBBLE_CLEAN,
        DISPENSER_MINECART
    }

}