package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.inventories.RedMerchantContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundSelectMerchantTradePacket implements NetworkHandler.Message {
    private final int item;

    public ServerBoundSelectMerchantTradePacket(FriendlyByteBuf buf) {
        this.item = buf.readVarInt();
    }

    public ServerBoundSelectMerchantTradePacket(int slot) {
        this.item = slot;
    }

    public static void buffer(ServerBoundSelectMerchantTradePacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.item);

    }

    public static void handler(ServerBoundSelectMerchantTradePacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        //ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER
        ctx.get().enqueueWork(() -> {
            AbstractContainerMenu container = ctx.get().getSender().containerMenu;

            int i = message.item;

            if (container instanceof RedMerchantContainer redMerchantContainer) {
                redMerchantContainer.setSelectionHint(i);
                redMerchantContainer.tryMoveItems(i);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}