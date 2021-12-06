package net.mehvahdjukaar.supplementaries.network;


import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
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
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> Minecraft.getInstance().player.setDeltaMovement(
                    Minecraft.getInstance().player.getDeltaMovement().add(msg.knockbackX, msg.knockbackY, msg.knockbackZ)));
        }

        ctx.get().setPacketHandled(true);
    }
}

