package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record ClientBoundLaunchCannonRiderPacket(Vec3 velocity) implements Message {
    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundLaunchCannonRiderPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_launch_cannon_rider"), ClientBoundLaunchCannonRiderPacket::new);

    public ClientBoundLaunchCannonRiderPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVec3());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVec3(this.velocity);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleLaunchCannonRiderPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
