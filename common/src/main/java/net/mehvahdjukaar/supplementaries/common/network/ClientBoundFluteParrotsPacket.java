package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

//does some on login stuff
public record ClientBoundFluteParrotsPacket(int playerId, boolean playing) implements Message {

    public ClientBoundFluteParrotsPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean());
    }

    public ClientBoundFluteParrotsPacket(Entity player, boolean started) {
        this(player.getId(), started);
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