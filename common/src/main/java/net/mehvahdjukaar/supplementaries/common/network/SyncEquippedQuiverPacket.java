package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;

public class SyncEquippedQuiverPacket implements Message {
    public final int entityID;
    public final ItemStack quiver;

    public SyncEquippedQuiverPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readVarInt();
        this.quiver = buf.readItem();
    }

    public <T extends Entity & IQuiverEntity> SyncEquippedQuiverPacket(T entity) {
        this.entityID = entity.getId();
        this.quiver = entity.supplementaries$getQuiver();
    }

    public SyncEquippedQuiverPacket(Entity entity, IQuiverEntity quiverEntity) {
        this.entityID = entity.getId();
        this.quiver = quiverEntity.supplementaries$getQuiver();
    }


    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityID);
        buf.writeItem(this.quiver);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //client received packet
        if (context.getDirection() == NetworkDir.PLAY_TO_SERVER) {
            //relay actual status to client
            Entity e = context.getSender().level().getEntity(entityID);
            if (e instanceof AbstractSkeleton && e instanceof IQuiverEntity qe && qe.supplementaries$hasQuiver()) {
                ModNetwork.CHANNEL.sentToAllClientPlayersTrackingEntity(e, new SyncEquippedQuiverPacket(e, qe));
            }
        } else ClientReceivers.handleSyncQuiverPacket(this);
    }


}