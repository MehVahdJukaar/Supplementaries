package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.world.songs.SongsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientBoundSetSongPacket {
    private final ResourceLocation song;
    private final UUID id;

    public ClientBoundSetSongPacket(FriendlyByteBuf buf) {
        this.song = buf.readResourceLocation();
        this.id = buf.readUUID();
    }

    public ClientBoundSetSongPacket(UUID id, ResourceLocation resourceLocation) {
        this.song = resourceLocation;
        this.id = id;
    }

    public static void buffer(ClientBoundSetSongPacket message, FriendlyByteBuf buf) {
        buf.writeResourceLocation(message.song);
        buf.writeUUID(message.id);
    }

    public static void handler(ClientBoundSetSongPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                SongsManager.setCurrentlyPlaying(message.id, message.song);
            });
        }
        ctx.get().setPacketHandled(true);
    }
}