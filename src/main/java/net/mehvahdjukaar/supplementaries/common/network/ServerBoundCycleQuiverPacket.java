package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundCycleQuiverPacket {
    private final int amount;
    private final boolean mainHand;
    private final boolean setSlot;

    public ServerBoundCycleQuiverPacket(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
        this.mainHand = buf.readBoolean();
        this.setSlot = buf.readBoolean();
    }

    public ServerBoundCycleQuiverPacket(int amount, boolean mainHand, boolean setSlot) {
        this.amount = amount;
        this.mainHand = mainHand;
        this.setSlot = setSlot;
    }

    public ServerBoundCycleQuiverPacket(int amount, boolean mainHand) {
        this(amount, mainHand, false); //cycle
    }

    public static void buffer(ServerBoundCycleQuiverPacket message, FriendlyByteBuf buf) {
        buf.writeInt(message.amount);
        buf.writeBoolean(message.mainHand);
        buf.writeBoolean(message.setSlot);
    }

    public static void handler(ServerBoundCycleQuiverPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // server world
            ServerPlayer player = (ServerPlayer) Objects.requireNonNull(ctx.get().getSender());
            if (player.getUsedItemHand() == InteractionHand.MAIN_HAND != message.mainHand) {
                int aa = 1; //this should not happen
            } else {
                ItemStack stack = player.getUseItem();
                if (stack.getItem() != ModRegistry.QUIVER_ITEM.get()) {
                    int aaa = 1;
                } else {
                    var data = QuiverItem.getQuiverData(stack);
                    if (message.setSlot) {
                        data.setSelectedSlot(message.amount);
                    } else {
                        data.cycle(message.amount);
                    }
                }
            }
        });
    }
}