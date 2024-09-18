package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record ServerBoundRequestOpenCannonGuiMessage(BlockPos pos) implements Message {

    public ServerBoundRequestOpenCannonGuiMessage(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        Player player = context.getSender();
        Level level = player.level();
        if (level.getBlockEntity(this.pos) instanceof CannonBlockTile tile) {
            tile.tryOpeningEditGui((ServerPlayer) player, this.pos, player.getMainHandItem());
        }

    }
}
