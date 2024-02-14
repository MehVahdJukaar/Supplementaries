package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class SyncSkellyQuiverPacket implements Message {
    public final int entityID;
    public final boolean on;

    public SyncSkellyQuiverPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readVarInt();
        this.on = buf.readBoolean();
    }

    public SyncSkellyQuiverPacket(AbstractSkeleton entity) {
        this.entityID = entity.getId();
        this.on = ((IQuiverEntity) entity).supplementaries$hasQuiver();
    }


    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityID);
        buf.writeBoolean(this.on);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //client received packet
        if (context.getDirection() == NetworkDir.PLAY_TO_SERVER) {
            //relay actual status to client
            Entity e = context.getSender().level().getEntity(entityID);
            if (e instanceof AbstractSkeleton q && e instanceof IQuiverEntity qe && qe.supplementaries$hasQuiver()) {
                NetworkHandler.CHANNEL.sentToAllClientPlayersTrackingEntity(e, new SyncSkellyQuiverPacket(q));
            }
        } else ClientReceivers.handleSyncQuiverPacket(this);
    }


}