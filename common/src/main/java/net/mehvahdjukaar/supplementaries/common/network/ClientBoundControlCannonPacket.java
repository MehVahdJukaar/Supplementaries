package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundControlCannonPacket(BlockPos pos) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundControlCannonPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_control_cannon"), ClientBoundControlCannonPacket::new);


    public ClientBoundControlCannonPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
          buf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(Context context) {
        // client world
        ClientReceivers.handleCannonControlPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}