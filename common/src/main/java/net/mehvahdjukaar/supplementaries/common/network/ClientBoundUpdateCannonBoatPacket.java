package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundUpdateCannonBoatPacket(TileOrEntityTarget target, CompoundTag tileTag) implements Message {

    public static final CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundUpdateCannonBoatPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_cannon_boat"), ClientBoundUpdateCannonBoatPacket::new);

    public ClientBoundUpdateCannonBoatPacket(RegistryFriendlyByteBuf buf) {
        this(TileOrEntityTarget.read(buf), buf.readNbt());
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        target.write(registryFriendlyByteBuf);
        registryFriendlyByteBuf.writeNbt(tileTag);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncCannonBoat(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
