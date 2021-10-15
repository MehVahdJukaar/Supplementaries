package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.inventories.RedMerchantContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectOrangeTraderTradePacket implements NetworkHandler.Message {
    private int item;

    public SelectOrangeTraderTradePacket(FriendlyByteBuf buf) {
        this.item = buf.readVarInt();
    }

    public SelectOrangeTraderTradePacket(int slot) {
        this.item = slot;
    }

    public static void buffer(SelectOrangeTraderTradePacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.item);

    }

    public static void handler(SelectOrangeTraderTradePacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        //ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER
        ctx.get().enqueueWork(() -> {
            AbstractContainerMenu container = ctx.get().getSender().containerMenu;

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