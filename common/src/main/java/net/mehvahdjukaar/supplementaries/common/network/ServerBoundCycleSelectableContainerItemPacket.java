package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ServerBoundCycleSelectableContainerItemPacket(int amount, Slot slot, boolean setSlot,
                                                            Item containerItem) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundCycleSelectableContainerItemPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_cycle_selectable_container_item"),
            ServerBoundCycleSelectableContainerItemPacket::new);

    public ServerBoundCycleSelectableContainerItemPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), Slot.values()[buf.readInt()], buf.readBoolean(), buf.readById(BuiltInRegistries.ITEM::byId));
    }

    public ServerBoundCycleSelectableContainerItemPacket(int amount, Slot slot, @NotNull SelectableContainerItem<?, ?> item) {
        this(amount, slot, false, item); //cycle
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeInt(this.slot.ordinal());
        buf.writeBoolean(this.setSlot);
        buf.writeById(BuiltInRegistries.ITEM::getId, this.containerItem);
    }

    @Override
    public void handle(Context context) {
        // server world
        if (containerItem instanceof SelectableContainerItem<?, ?> instance) {
            ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getPlayer());
            ItemStack stack = ItemStack.EMPTY;
            if (slot == Slot.INVENTORY) {
                stack = instance.getFirstInInventory(player);
            } else if (player.getUsedItemHand() == InteractionHand.MAIN_HAND == (slot == Slot.MAIN_HAND)) {
                stack = player.getUseItem();
            }
            if (!(stack.getItem() instanceof SelectableContainerItem<?, ?> item)) {
                Supplementaries.error(); //should not happen
            } else {
                doStuffTyped(item.getComponentType(), stack);
            }
        } else Supplementaries.error();
    }

    private <T extends SelectableContainerContent<M>, M extends SelectableContainerContent.Mut<T>> void doStuffTyped(
            DataComponentType<T> key, ItemStack stack) {
        var data = stack.get(key);
        if (data != null) {
            var mutable = data.toMutable();
            if (setSlot) {
                mutable.setSelectedSlot(amount);
            } else {
                mutable.cycle(amount);
            }
            stack.set(key, mutable.toImmutable());
        }
    }

    public enum Slot {
        MAIN_HAND,
        OFF_HAND,
        INVENTORY
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}