package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ServerBoundCycleSelectableContainerItemPacket(int amount,SlotReference slotReference, boolean setSlot) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundCycleSelectableContainerItemPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_cycle_selectable_container_item"),
            ServerBoundCycleSelectableContainerItemPacket::new);

    public ServerBoundCycleSelectableContainerItemPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), SlotReference.STREAM_CODEC.decode(buf) ,buf.readBoolean());
    }

    public ServerBoundCycleSelectableContainerItemPacket(int amount, SlotReference slot) {
        this(amount, slot, false); //cycle
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        SlotReference.STREAM_CODEC.encode(buf, this.slotReference);
        buf.writeBoolean(this.setSlot);
    }

    @Override
    public void handle(Context context) {
        // server world

        if (context.getPlayer() instanceof ServerPlayer player) {
            ItemStack stack = slotReference.get(player);
            if (!(stack.getItem() instanceof SelectableContainerItem<?, ?> item)) {
                Supplementaries.error(); //should not happen
            } else {
                item.modify(stack, data -> {
                    if (setSlot) {
                        data.setSelectedSlot(amount);
                    } else {
                        data.cycle(amount);
                    }
                    return true;
                });
            }
        } else Supplementaries.error();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}