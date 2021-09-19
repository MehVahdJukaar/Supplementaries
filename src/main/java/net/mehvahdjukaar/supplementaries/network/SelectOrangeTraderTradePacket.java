package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.inventories.RedMerchantContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectOrangeTraderTradePacket implements NetworkHandler.Message {
    private int item;

    public SelectOrangeTraderTradePacket(PacketBuffer buf) {
        this.item = buf.readVarInt();
    }

    public SelectOrangeTraderTradePacket(int slot) {
        this.item = slot;
    }

    public static void buffer(SelectOrangeTraderTradePacket message, PacketBuffer buf) {
        buf.writeVarInt(message.item);

    }

    public static void handler(SelectOrangeTraderTradePacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        //ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER
        ctx.get().enqueueWork(() -> {
            Container container = ctx.get().getSender().containerMenu;

            int i = message.item;

            if (container instanceof RedMerchantContainer) {
                RedMerchantContainer merchantcontainer = (RedMerchantContainer)container;
                merchantcontainer.setSelectionHint(i);
                merchantcontainer.tryMoveItems(i);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}