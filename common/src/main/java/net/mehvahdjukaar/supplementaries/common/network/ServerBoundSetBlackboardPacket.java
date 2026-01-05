package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ServerBoundSetBlackboardPacket implements Message {
    private final BlockPos pos;
    private final byte[][] pixels;

    public ServerBoundSetBlackboardPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.pixels = new byte[16][16];
        for (int i = 0; i < this.pixels.length; i++) {
            this.pixels[i] = buf.readByteArray();
        }
    }

    public ServerBoundSetBlackboardPacket(BlockPos pos, byte[][] pixels) {
        this.pos = pos;
        this.pixels = pixels;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        for (byte[] pixel : this.pixels) {
            buf.writeByteArray(pixel);
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        if (context.getSender() instanceof ServerPlayer player) {
            Level level = player.level();

            BlockPos pos = this.pos;
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof BlackboardBlockTile board) {
                if (board.tryAcceptingClientPixels(player, this.pixels)) {
                    //updates client
                    //set changed also sends a block update
                    board.setChanged();
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                }
            }
        }
    }
}