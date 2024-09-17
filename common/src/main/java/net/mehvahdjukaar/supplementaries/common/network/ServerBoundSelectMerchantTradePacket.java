package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.inventories.RedMerchantMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ServerBoundSelectMerchantTradePacket(int item) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSelectMerchantTradePacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_select_merchant_trade"), ServerBoundSelectMerchantTradePacket::new);

    public ServerBoundSelectMerchantTradePacket(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.item);
    }

    @Override
    public void handle(Context context) {
        AbstractContainerMenu container = context.getPlayer().containerMenu;

        int i = this.item;

        if (container instanceof RedMerchantMenu redMerchantContainerMenu) {
            redMerchantContainerMenu.setSelectionHint(i);
            redMerchantContainerMenu.tryMoveItems(i);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
