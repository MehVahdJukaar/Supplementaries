package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.block.util.ICustomDataHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class NosePacket implements NetworkHandler.Message {

    private int id;
    private boolean on;

    public NosePacket(int id, boolean on) {
        this.id = id;
        this.on = on;
    }

    public static void buffer(NosePacket pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.on);
        buf.writeInt(pkt.id);
    }

    public NosePacket(PacketBuffer buf) {
        this.on = buf.readBoolean();
        this.id = buf.readInt();
    }


    public static void handler(NosePacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {

                Entity entity = Minecraft.getInstance().level.getEntity(msg.id);
                if(entity instanceof ICustomDataHolder){
                    ((ICustomDataHolder) entity).setVariable(msg.on);
                }

            });
        }

        ctx.get().setPacketHandled(true);
    }
}

