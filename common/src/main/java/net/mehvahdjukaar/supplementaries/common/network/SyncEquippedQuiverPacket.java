package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;

public record SyncEquippedQuiverPacket(int entityID, ItemStack heldQuiver) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncEquippedQuiverPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_equipped_quiver"), SyncEquippedQuiverPacket::new);

    public SyncEquippedQuiverPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    public <A extends Entity & IQuiverEntity> SyncEquippedQuiverPacket(A entity) {
        this(entity, entity);
    }

    public SyncEquippedQuiverPacket(Entity entity, IQuiverEntity qe) {
        this(entity.getId(), qe.supplementaries$getQuiver());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entityID);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, heldQuiver);
    }

    @Override
    public void handle(Context context) {
        //client received packet
        if (context.getDirection() == NetworkDir.SERVER_BOUND) {
            //relay actual status to client
            Entity e = context.getPlayer().level().getEntity(entityID);
            if (e instanceof AbstractSkeleton q && e instanceof IQuiverEntity qe && qe.supplementaries$hasQuiver()) {
                NetworkHelper.sendToAllClientPlayersTrackingEntity(e, new SyncEquippedQuiverPacket(q, qe));
            }
        } else ClientReceivers.handleSyncQuiverPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}