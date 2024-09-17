package net.mehvahdjukaar.supplementaries.common.network;


import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;

public record ClientBoundPlaySongNotesPacket(IntList notes, int entityID) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundPlaySongNotesPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_play_song_notes"), ClientBoundPlaySongNotesPacket::new);

    public ClientBoundPlaySongNotesPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readIntIdList(), buf.readVarInt());
    }

    public ClientBoundPlaySongNotesPacket(IntList notes, LivingEntity player) {
        this(notes, player.getId());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entityID);
        buf.writeIntIdList(this.notes);
    }

    @Override
    public void handle(Context context) {
        // client world
        ClientReceivers.handlePlaySongNotesPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}