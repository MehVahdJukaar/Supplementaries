package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record ClientBoundSendKnockbackPacket(int id, Vec3 knockback) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendKnockbackPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_send_knockback"), ClientBoundSendKnockbackPacket::new);

    public ClientBoundSendKnockbackPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVec3());
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeVarInt(this.id);
        registryFriendlyByteBuf.writeVec3(this.knockback);
    }

    @Override
    public void handle(Context context) {
        // client world
        ClientReceivers.handleSendBombKnockbackPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}

