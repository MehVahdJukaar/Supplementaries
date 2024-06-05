package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

//does some on login stuff
public class ClientBoundFluteParrotsPacket implements Message {

    public final int playerId;
    public final boolean playing;

    public ClientBoundFluteParrotsPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readVarInt();
        this.playing = buf.readBoolean();
    }

    public ClientBoundFluteParrotsPacket(Entity player, boolean started) {
        this.playerId = player.getId();
        this.playing = started;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(this.playerId);
        buf.writeBoolean(this.playing);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleParrotPacket(this);
    }
}