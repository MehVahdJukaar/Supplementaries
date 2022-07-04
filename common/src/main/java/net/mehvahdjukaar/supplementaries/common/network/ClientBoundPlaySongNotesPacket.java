package net.mehvahdjukaar.supplementaries.common.network;


import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundPlaySongNotesPacket implements NetworkHandler.Message {
    public final IntList notes;
    public final int entityID;

    public ClientBoundPlaySongNotesPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readVarInt();
        this.notes = buf.readIntIdList();

    }

    public ClientBoundPlaySongNotesPacket(IntList notes, LivingEntity player) {
        this.entityID = player.getId();
        this.notes = notes;
    }

    public static void buffer(ClientBoundPlaySongNotesPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.entityID);
        buf.writeIntIdList(message.notes);
    }

    public static void handler(ClientBoundPlaySongNotesPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {

                ClientReceivers.handlePlaySongNotesPacket(message);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}