package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.inventories.RedMerchantContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendOrangeTraderOffersPacket {
    private int containerId;
    public MerchantOffers offers;
    private int villagerLevel;
    private int villagerXp;
    private boolean showProgress;
    private boolean canRestock;
    public SendOrangeTraderOffersPacket(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.offers = MerchantOffers.createFromStream(buf);
        this.villagerLevel = buf.readVarInt();
        this.villagerXp = buf.readVarInt();
        this.showProgress = buf.readBoolean();
        this.canRestock = buf.readBoolean();
    }

    public SendOrangeTraderOffersPacket(int id, MerchantOffers offers, int level, int villagerXp, boolean showProgress, boolean canRestock) {
        this.containerId = id;
        this.offers = offers;
        this.villagerLevel = level;
        this.villagerXp = villagerXp;
        this.showProgress = showProgress;
        this.canRestock = canRestock;
    }

    public static void buffer(SendOrangeTraderOffersPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.containerId);
        message.offers.writeToStream(buf);
        buf.writeVarInt(message.villagerLevel);
        buf.writeVarInt(message.villagerXp);
        buf.writeBoolean(message.showProgress);
        buf.writeBoolean(message.canRestock);

    }

    public static void handler(SendOrangeTraderOffersPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        ctx.get().enqueueWork(() -> {
            AbstractContainerMenu container = Minecraft.getInstance().player.containerMenu;
            if (message.containerId == container.containerId && container instanceof RedMerchantContainer) {
                ((RedMerchantContainer)container).setOffers(new MerchantOffers(message.offers.createTag()));
                ((RedMerchantContainer)container).setXp(message.villagerXp);
                ((RedMerchantContainer)container).setMerchantLevel(message.villagerLevel);
                ((RedMerchantContainer)container).setShowProgressBar(message.showProgress);
                ((RedMerchantContainer)container).setCanRestock(message.canRestock);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}