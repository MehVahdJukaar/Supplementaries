package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ServerBoundCycleQuiverPacket implements Message {
    private final int amount;
    private final Slot slot;
    private final boolean setSlot;

    public ServerBoundCycleQuiverPacket(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
        this.slot = Slot.values()[buf.readInt()];
        this.setSlot = buf.readBoolean();
    }

    public ServerBoundCycleQuiverPacket(int amount, Slot slot, boolean setSlot) {
        this.amount = amount;
        this.slot = slot;
        this.setSlot = setSlot;
    }

    public ServerBoundCycleQuiverPacket(int amount, Slot slot) {
        this(amount, slot, false); //cycle
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeInt(this.slot.ordinal());
        buf.writeBoolean(this.setSlot);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server world
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getSender());
        ItemStack stack = ItemStack.EMPTY;
        if (slot == Slot.INVENTORY) {
            stack = QuiverItem.getQuiver(player);
        } else if (player.getUsedItemHand() == InteractionHand.MAIN_HAND == (slot == Slot.MAIN_HAND)) {
            stack = player.getUseItem();
        }
        if (stack.getItem() != ModRegistry.QUIVER_ITEM.get()) {
            Supplementaries.error(); //should not happen
        } else {
            var data = QuiverItem.getQuiverData(stack);
            if (setSlot) {
                data.setSelectedSlot(amount);
            } else {
                data.cycle(amount);
            }
        }
    }

    public enum Slot {
        MAIN_HAND,
        OFF_HAND,
        INVENTORY
    }
}