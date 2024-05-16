package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ServerBoundLunchBoxRightClickedPacket implements Message {
    private final InteractionHand hand;

    public ServerBoundLunchBoxRightClickedPacket(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);
    }

    public ServerBoundLunchBoxRightClickedPacket(InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
    }

    @Override
    public void handle(ChannelHandler.Context context) {

        // server level
        Player player = Objects.requireNonNull(context.getSender());

        ItemStack item = player.getItemInHand(hand);
        if (item.getItem() instanceof LunchBoxItem li) {
            li.getData(item).switchMode();
        }
    }
}