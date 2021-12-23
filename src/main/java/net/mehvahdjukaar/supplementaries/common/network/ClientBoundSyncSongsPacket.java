package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.common.world.songs.Song;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ClientBoundSyncSongsPacket {

    protected final Map<ResourceLocation, Song> songs;

    public ClientBoundSyncSongsPacket(final Map<ResourceLocation, Song> songs) {
        this.songs = songs;
    }

    public ClientBoundSyncSongsPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.songs = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation name = buf.readResourceLocation();
            CompoundTag tag = buf.readNbt();
            if (tag != null) {
                Song song = Song.loadFromTag(tag);
                songs.put(name, song);
            }
        }
    }

    public static void buffer(ClientBoundSyncSongsPacket message, final FriendlyByteBuf buf) {
        buf.writeInt(message.songs.size());
        for (var entry : message.songs.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeNbt(Song.saveToTag(entry.getValue()));
        }
    }

    public static void handler(ClientBoundSyncSongsPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                SongsManager.clearSongs();
                message.songs.keySet().forEach(k -> {
                    Song s = message.songs.get(k);
                    s.processForPlaying();
                    SongsManager.addSong(k, s);
                });
            }
        });

        context.setPacketHandled(true);
    }

}
