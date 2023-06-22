package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class ClientBoundPlaySpeakerMessagePacket implements Message {

    public final Component message;
    public final Component filtered;
    public final SpeakerBlockTile.Mode mode;

    public ClientBoundPlaySpeakerMessagePacket(FriendlyByteBuf buf) {
        this.message = buf.readComponent();
        this.filtered = buf.readComponent();
        this.mode = SpeakerBlockTile.Mode.values()[buf.readByte()];
    }

    public ClientBoundPlaySpeakerMessagePacket(Component message, Component filtered, SpeakerBlockTile.Mode mode) {
        this.message = message;
        this.filtered = filtered;
        this.mode = mode;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeComponent(this.message);
        buf.writeComponent(this.filtered);
        buf.writeByte(this.mode.ordinal());
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // client world
        ClientReceivers.handlePlaySpeakerMessagePacket(this);
    }
}