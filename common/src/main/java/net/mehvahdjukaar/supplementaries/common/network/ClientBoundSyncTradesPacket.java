package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientBoundSyncTradesPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncTradesPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_trades"), ClientBoundSyncTradesPacket::new);

    public final int containerId;
    public final MerchantOffers offers;
    public final int villagerLevel;
    public final int villagerXp;
    public final boolean showProgress;
    public final boolean canRestock;

    public ClientBoundSyncTradesPacket(RegistryFriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.offers = MerchantOffers.STREAM_CODEC.decode(buf);
        this.villagerLevel = buf.readVarInt();
        this.villagerXp = buf.readVarInt();
        this.showProgress = buf.readBoolean();
        this.canRestock = buf.readBoolean();
    }

    public ClientBoundSyncTradesPacket(int id, MerchantOffers offers, int level, int villagerXp, boolean showProgress, boolean canRestock) {
        this.containerId = id;
        this.offers = offers;
        this.villagerLevel = level;
        this.villagerXp = villagerXp;
        this.showProgress = showProgress;
        this.canRestock = canRestock;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        MerchantOffers.STREAM_CODEC.encode(buf, this.offers);
        buf.writeVarInt(this.villagerLevel);
        buf.writeVarInt(this.villagerXp);
        buf.writeBoolean(this.showProgress);
        buf.writeBoolean(this.canRestock);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncTradesPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}