package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class ClientBoundPlaySpeakerMessagePacket implements Message {

    public final Component str;
    public final boolean narrator;

    public ClientBoundPlaySpeakerMessagePacket(FriendlyByteBuf buf) {
        this.str = buf.readComponent();
        this.narrator = buf.readBoolean();
    }

    public ClientBoundPlaySpeakerMessagePacket(Component str, boolean narrator) {
        this.str = str;
        this.narrator = narrator;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeComponent(this.str);
        buf.writeBoolean(this.narrator);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // client world
        ClientReceivers.handlePlaySpeakerMessagePacket(this);
    }
}