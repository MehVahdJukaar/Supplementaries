package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientBoundPlaySpeakerMessagePacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundPlaySpeakerMessagePacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_play_speaker_message"), ClientBoundPlaySpeakerMessagePacket::new);

    public final Component message;
    public final Component filtered;
    public final SpeakerBlockTile.Mode mode;

    public ClientBoundPlaySpeakerMessagePacket(RegistryFriendlyByteBuf buf) {
        this.message = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
        this.filtered = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
        this.mode = SpeakerBlockTile.Mode.values()[buf.readByte()];
    }

    public ClientBoundPlaySpeakerMessagePacket(Component message, Component filtered, SpeakerBlockTile.Mode mode) {
        this.message = message;
        this.filtered = filtered;
        this.mode = mode;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, this.message);
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, this.filtered);
        buf.writeByte(this.mode.ordinal());
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handlePlaySpeakerMessagePacket(this);

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}