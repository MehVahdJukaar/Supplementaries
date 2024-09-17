package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;

//does some on login stuff
public record ClientBoundFluteParrotsPacket(int playerId, boolean playing) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundFluteParrotsPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_flute_parrots"), ClientBoundFluteParrotsPacket::new);

    public ClientBoundFluteParrotsPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean());
    }

    public ClientBoundFluteParrotsPacket(Entity player, boolean started) {
        this(player.getId(), started);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.playerId);
        buf.writeBoolean(this.playing);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleParrotPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}