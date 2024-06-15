package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record ClientBoundSetSongPacket(String song, UUID id) implements Message {

    public ClientBoundSetSongPacket(FriendlyByteBuf buf) {
          this(buf.readUtf(), buf.readUUID());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeUtf(this.song);
        buf.writeUUID(this.id);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // client world
        SongsManager.setCurrentlyPlaying(this.id, this.song);

    }
}