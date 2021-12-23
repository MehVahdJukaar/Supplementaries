package net.mehvahdjukaar.supplementaries.common.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSendBombKnockbackPacket implements NetworkHandler.Message {

    private final double knockbackX;
    private final double knockbackY;
    private final double knockbackZ;

    public ClientBoundSendBombKnockbackPacket(Vec3 knockback) {
        this.knockbackX = knockback.x;
        this.knockbackY = knockback.y;
        this.knockbackZ = knockback.z;
    }

    public static void buffer(ClientBoundSendBombKnockbackPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.knockbackX);
        buf.writeDouble(pkt.knockbackY);
        buf.writeDouble(pkt.knockbackZ);

    }

    public ClientBoundSendBombKnockbackPacket(FriendlyByteBuf buf) {
        this.knockbackX = buf.readDouble();
        this.knockbackY = buf.readDouble();
        this.knockbackZ = buf.readDouble();
    }


    public static void handler(ClientBoundSendBombKnockbackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSendBombKnockbackPacket(msg);
            }
        });

        ctx.get().setPacketHandled(true);
    }

    public double getKnockbackX() {
        return knockbackX;
    }

    public double getKnockbackY() {
        return knockbackY;
    }

    public double getKnockbackZ() {
        return knockbackZ;
    }
}

