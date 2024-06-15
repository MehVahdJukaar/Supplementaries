package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record ServerBoundLunchBoxRightClickedPacket(InteractionHand hand) implements Message {

    public ServerBoundLunchBoxRightClickedPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(InteractionHand.class));
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