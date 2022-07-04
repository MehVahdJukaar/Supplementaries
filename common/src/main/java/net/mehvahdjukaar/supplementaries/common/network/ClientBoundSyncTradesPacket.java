package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientBoundSyncTradesPacket implements Message {
    public final int containerId;
    public final MerchantOffers offers;
    public final int villagerLevel;
    public final int villagerXp;
    public final boolean showProgress;
    public final boolean canRestock;

    public ClientBoundSyncTradesPacket(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.offers = MerchantOffers.createFromStream(buf);
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
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        this.offers.writeToStream(buf);
        buf.writeVarInt(this.villagerLevel);
        buf.writeVarInt(this.villagerXp);
        buf.writeBoolean(this.showProgress);
        buf.writeBoolean(this.canRestock);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleSyncTradesPacket(this);
    }

}