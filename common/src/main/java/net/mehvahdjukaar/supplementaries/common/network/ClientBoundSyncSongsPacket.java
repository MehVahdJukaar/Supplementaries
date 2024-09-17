package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.songs.Song;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientBoundSyncSongsPacket implements Message {

    protected final List<Song> songs;

    public ClientBoundSyncSongsPacket(final Collection<Song> songs) {
        this.songs = List.copyOf(songs);
    }

    public ClientBoundSyncSongsPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.songs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag != null) {
                //TODO: stream codec there
                var r = Song.CODEC.parse(NbtOps.INSTANCE, tag);
                r.result().ifPresent(songs::add);
            }
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(this.songs.size());
        for (var entry : this.songs) {
            var r = Song.CODEC.encodeStart(NbtOps.INSTANCE, entry);
            if (r.result().isPresent()) {
                buf.writeNbt((CompoundTag) r.result().get());
            }
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //client world
        SongsManager.acceptClientSongs(this.songs);
        Supplementaries.LOGGER.info("Synced Flute Songs");
    }


}
