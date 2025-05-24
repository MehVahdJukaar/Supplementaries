package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundCannonAnimationPacket(TileOrEntityTarget target, boolean fire) implements Message {


    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundCannonAnimationPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_cannon_particle"), ClientBoundCannonAnimationPacket::new);

    public ClientBoundCannonAnimationPacket(RegistryFriendlyByteBuf buf) {
        this(TileOrEntityTarget.read(buf), buf.readBoolean());
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.target.write(registryFriendlyByteBuf);
        registryFriendlyByteBuf.writeBoolean(this.fire);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleCannonAnimation(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
