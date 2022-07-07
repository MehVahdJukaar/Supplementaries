package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.world.songs.Song;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ClientBoundSyncSongsPacket implements Message {

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

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(this.songs.size());
        for (var entry : this.songs.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeNbt(Song.saveToTag(entry.getValue()));
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //client world
        SongsManager.clearSongs();
        this.songs.keySet().forEach(k -> {
            Song s = this.songs.get(k);
            s.processForPlaying();
            SongsManager.addSong(k, s);
        });

    }

}
