package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ClientBoundSetSongPacket(String song, UUID id) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSetSongPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_set_song"), ClientBoundSetSongPacket::new);

    public ClientBoundSetSongPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUUID());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.song);
        buf.writeUUID(this.id);
    }

    @Override
    public void handle(Context context) {
        // client world
        SongsManager.setCurrentlyPlaying(this.id, this.song);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}