package net.mehvahdjukaar.supplementaries.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSyncTradesPacket {
    private final int containerId;
    public final MerchantOffers offers;
    private final int villagerLevel;
    private final int villagerXp;
    private final boolean showProgress;
    private final boolean canRestock;

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

    public static void buffer(ClientBoundSyncTradesPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.containerId);
        message.offers.writeToStream(buf);
        buf.writeVarInt(message.villagerLevel);
        buf.writeVarInt(message.villagerXp);
        buf.writeBoolean(message.showProgress);
        buf.writeBoolean(message.canRestock);

    }

    public static void handler(ClientBoundSyncTradesPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSyncTradesPacket(message);
            }
        });

        ctx.get().setPacketHandled(true);
    }

    public int getContainerId() {
        return containerId;
    }

    public int getVillagerLevel() {
        return villagerLevel;
    }

    public int getVillagerXp() {
        return villagerXp;
    }

    public MerchantOffers getOffers() {
        return offers;
    }

    public boolean isCanRestock() {
        return canRestock;
    }

    public boolean isShowProgress() {
        return showProgress;
    }
}