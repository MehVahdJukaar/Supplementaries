package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.client.renderers.entities.PicklePlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class PicklePacket {

    private UUID playerID;
    private boolean on;

    public PicklePacket(UUID appliesTo, boolean on) {
        this.playerID = appliesTo;
        this.on = on;
    }

    public static void buffer(PicklePacket pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.on);
        if (pkt.playerID != null) {
            buf.writeUUID(pkt.playerID);
        }
    }

    public PicklePacket(PacketBuffer buf) {
        this.on = buf.readBoolean();
        if (buf.isReadable()) {
            this.playerID = buf.readUUID();
        }
    }


    public static void handler(PicklePacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            //receive broadcasted message
            ctx.get().enqueueWork(() -> PicklePlayer.PickleData.set(msg.playerID, msg.on));
        }
        else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                //gets id from server just to be sure
                PlayerEntity player = ctx.get().getSender();
                UUID id = player.getGameProfile().getId();
                if (PicklePlayer.PickleData.isDev(id)) {

                    //stores value server side
                    PicklePlayer.PickleData.set(id, msg.on);
                    msg.playerID = id;
                    //broadcast to all players
                    for (ServerPlayerEntity p : player.getServer().getPlayerList().getPlayers()) {
                        if (p != player) {
                            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), msg);
                        }
                    }
                }

            });
        }

        ctx.get().setPacketHandled(true);
    }
}

