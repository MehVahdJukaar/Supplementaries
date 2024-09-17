package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public record SyncSkellyQuiverPacket(int entityID, boolean on) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncSkellyQuiverPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_skelly_quiver"), SyncSkellyQuiverPacket::new);

    public SyncSkellyQuiverPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean());
    }

    public SyncSkellyQuiverPacket(AbstractSkeleton entity) {
        this(entity.getId(), entity instanceof IQuiverEntity qe && qe.supplementaries$hasQuiver());
    }


    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entityID);
        buf.writeBoolean(this.on);
    }

    @Override
    public void handle(Context context) {
        //client received packet
        if (context.getDirection() == NetworkDir.SERVER_BOUND) {
            //relay actual status to client
            Entity e = context.getPlayer().level().getEntity(entityID);
            if (e instanceof AbstractSkeleton q && e instanceof IQuiverEntity qe && qe.supplementaries$hasQuiver()) {
                NetworkHelper.sendToAllClientPlayersTrackingEntity(e, new SyncSkellyQuiverPacket(q));
            }
        } else ClientReceivers.handleSyncQuiverPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}