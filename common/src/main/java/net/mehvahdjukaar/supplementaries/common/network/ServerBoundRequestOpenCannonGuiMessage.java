package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public record ServerBoundRequestOpenCannonGuiMessage(TileOrEntityTarget target) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundRequestOpenCannonGuiMessage> CODEC = Message.makeType(
            Supplementaries.res("c2s_request_open_cannon_gui"), ServerBoundRequestOpenCannonGuiMessage::new);

    public ServerBoundRequestOpenCannonGuiMessage(RegistryFriendlyByteBuf buf) {
        this(TileOrEntityTarget.read(buf));
    }

    @Override
    public void write(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.target.write(friendlyByteBuf);
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer() instanceof ServerPlayer player) {
            ItemStack stack = player.getMainHandItem();
            Utils.openGuiIfPossible(target, player, stack, Direction.NORTH, Vec3.ZERO);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
