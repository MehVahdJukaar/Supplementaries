package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ServerBoundCycleSelectableContainerItemPacket implements Message {
    private final int amount;
    private final Slot slot;
    private final boolean setSlot;
    private final Item itemInstance; //send data here in 1.20.6

    public ServerBoundCycleSelectableContainerItemPacket(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
        this.slot = Slot.values()[buf.readInt()];
        this.setSlot = buf.readBoolean();
        this.itemInstance = buf.readById(BuiltInRegistries.ITEM);
    }

    public ServerBoundCycleSelectableContainerItemPacket(int amount, Slot slot, boolean setSlot,
                                                         SelectableContainerItem<?,?> item) {
        this.amount = amount;
        this.slot = slot;
        this.setSlot = setSlot;
        this.itemInstance = item;
    }

    public ServerBoundCycleSelectableContainerItemPacket(int amount, Slot slot, @NotNull SelectableContainerItem<?,?> item) {
        this(amount, slot, false, item); //cycle
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeInt(this.slot.ordinal());
        buf.writeBoolean(this.setSlot);
        buf.writeId(BuiltInRegistries.ITEM, this.itemInstance);
    }

    @Override
    public void handle(Context context) {
        // server world
        if (itemInstance instanceof SelectableContainerItem<?> instance) {
            ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getSender());
            ItemStack stack = ItemStack.EMPTY;
            if (slot == Slot.INVENTORY) {
                stack = instance.getFirstInInventory(player);
            } else if (player.getUsedItemHand() == InteractionHand.MAIN_HAND == (slot == Slot.MAIN_HAND)) {
                stack = player.getUseItem();
            }
            if (!(stack.getItem() instanceof SelectableContainerItem<?> item)) {
                Supplementaries.error(); //should not happen
            } else {
                var data = item.getComponentType(stack);
                if (setSlot) {
                    data.setSelectedSlot(amount);
                } else {
                    data.cycle(amount);
                }
            }
        } else Supplementaries.error();
    }

    public enum Slot {
        MAIN_HAND,
        OFF_HAND,
        INVENTORY
    }
}