package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.pathfinder.Path;

public record ClientBoundDebugPathfindingPacket(int entityId, Path path, float maxNodeDistance) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundDebugPathfindingPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_debug_pathfinding"), ClientBoundDebugPathfindingPacket::new);

    private ClientBoundDebugPathfindingPacket(RegistryFriendlyByteBuf buffer) {
        this(buffer.readVarInt(), true ? null : Path.createFromStream(buffer), buffer.readFloat());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        this.path.writeToStream(buffer);
        buffer.writeFloat(this.maxNodeDistance);
    }

    @Override
    public void handle(Context context) {
      //  ClientReceivers.handleDebugNav(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
