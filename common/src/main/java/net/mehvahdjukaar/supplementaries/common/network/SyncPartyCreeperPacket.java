package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

public record SyncPartyCreeperPacket(int entityID, boolean on) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncPartyCreeperPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_party_creeper"), SyncPartyCreeperPacket::new);

    public SyncPartyCreeperPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean());
    }

    public SyncPartyCreeperPacket(LivingEntity entity) {
        this(entity.getId(), entity instanceof IPartyCreeper pc && pc.supplementaries$isFestive());
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
            if (e instanceof Creeper c && e instanceof IPartyCreeper pc && pc.supplementaries$isFestive()) {
                NetworkHelper.sendToAllClientPlayersTrackingEntity(e, new SyncPartyCreeperPacket(c));
            }
        } else ClientReceivers.handleSyncPartyCreeper(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}