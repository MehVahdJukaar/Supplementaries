package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ClientBoundSetSongPacket implements Message {
    private final String song;
    private final UUID id;

    public ClientBoundSetSongPacket(FriendlyByteBuf buf) {
        this.song = buf.readUtf();
        this.id = buf.readUUID();
    }

    public ClientBoundSetSongPacket(UUID id, String s) {
        this.song = s;
        this.id = id;
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