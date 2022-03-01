package net.mehvahdjukaar.supplementaries.common.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSendKnockbackPacket implements NetworkHandler.Message {

    public final int id;
    public final double knockbackX;
    public final double knockbackY;
    public final double knockbackZ;

    public ClientBoundSendKnockbackPacket(Vec3 knockback, int id) {
        this.id = id;
        this.knockbackX = knockback.x;
        this.knockbackY = knockback.y;
        this.knockbackZ = knockback.z;
    }

    public static void buffer(ClientBoundSendKnockbackPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.id);
        buf.writeDouble(pkt.knockbackX);
        buf.writeDouble(pkt.knockbackY);
        buf.writeDouble(pkt.knockbackZ);

    }

    public ClientBoundSendKnockbackPacket(FriendlyByteBuf buf) {
        this.id = buf.readInt();
        this.knockbackX = buf.readDouble();
        this.knockbackY = buf.readDouble();
        this.knockbackZ = buf.readDouble();
    }


    public static void handler(ClientBoundSendKnockbackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSendBombKnockbackPacket(msg);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}

