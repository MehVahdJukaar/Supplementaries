package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public record ServerBoundRequestOpenCannonGuiMessage(BlockPos pos) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundRequestOpenCannonGuiMessage> CODEC = Message.makeType(
            Supplementaries.res("c2s_request_open_cannon_gui"), ServerBoundRequestOpenCannonGuiMessage::new);

    public ServerBoundRequestOpenCannonGuiMessage(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public void write(RegistryFriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer() instanceof ServerPlayer player) {
            Level level = player.level();
            if (level.getBlockEntity(this.pos) instanceof CannonBlockTile tile) {
                tile.tryOpeningEditGui(player, this.pos, player.getMainHandItem());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
