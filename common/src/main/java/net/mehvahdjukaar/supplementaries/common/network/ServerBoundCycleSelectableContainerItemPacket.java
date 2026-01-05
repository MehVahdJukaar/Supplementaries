package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record ServerBoundCycleSelectableContainerItemPacket(int amount, SlotReference slotReference, boolean setSlot)
        implements Message {

    public ServerBoundCycleSelectableContainerItemPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), SlotReference.decode(buf), buf.readBoolean());
    }

    public ServerBoundCycleSelectableContainerItemPacket(int amount, SlotReference slot) {
        this(amount, slot, false); //cycle
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        SlotReference.encode(buf, this.slotReference);
        buf.writeBoolean(this.setSlot);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server world

        if (context.getSender() instanceof ServerPlayer player) {
            ItemStack stack = slotReference.get(player);
            if (!(stack.getItem() instanceof SelectableContainerItem item)) {
                Supplementaries.error(); //should not happen
            } else {
                var data = item.getData(stack);
                if (setSlot) {
                    data.setSelectedSlot(amount);
                } else {
                    data.cycle(amount);
                }
            }
        } else Supplementaries.error();
    }

}