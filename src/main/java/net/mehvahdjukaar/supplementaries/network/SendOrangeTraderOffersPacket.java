package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.inventories.OrangeMerchantContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendOrangeTraderOffersPacket {
    private int containerId;
    public MerchantOffers offers;
    private int villagerLevel;
    private int villagerXp;
    private boolean showProgress;
    private boolean canRestock;
    public SendOrangeTraderOffersPacket(PacketBuffer buf) {
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

    public static void buffer(SendOrangeTraderOffersPacket message, PacketBuffer buf) {
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
            Container container = Minecraft.getInstance().player.containerMenu;
            if (message.containerId == container.containerId && container instanceof OrangeMerchantContainer) {
                ((OrangeMerchantContainer)container).setOffers(new MerchantOffers(message.offers.createTag()));
                ((OrangeMerchantContainer)container).setXp(message.villagerXp);
                ((OrangeMerchantContainer)container).setMerchantLevel(message.villagerLevel);
                ((OrangeMerchantContainer)container).setShowProgressBar(message.showProgress);
                ((OrangeMerchantContainer)container).setCanRestock(message.canRestock);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}