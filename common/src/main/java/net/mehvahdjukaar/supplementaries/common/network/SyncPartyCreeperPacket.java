package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;

public class SyncPartyCreeperPacket implements Message {
    public final int entityID;
    public final boolean on;

    public SyncPartyCreeperPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readVarInt();
        this.on = buf.readBoolean();
    }

    public SyncPartyCreeperPacket(LivingEntity entity) {
        this.entityID = entity.getId();
        this.on = ((IPartyCreeper) entity).supplementaries$isFestive();
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
            if (e instanceof Creeper c && e instanceof IPartyCreeper pc && pc.supplementaries$isFestive()) {
                ModNetwork.CHANNEL.sentToAllClientPlayersTrackingEntity(e, new SyncPartyCreeperPacket(c));
            }
        } else ClientReceivers.handleSyncPartyCreeper(this);
    }


}