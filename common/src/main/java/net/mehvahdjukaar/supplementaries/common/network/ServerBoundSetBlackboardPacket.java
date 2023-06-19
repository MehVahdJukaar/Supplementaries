package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Objects;

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

        // server level
        Level level = Objects.requireNonNull(context.getSender()).level();

        BlockPos pos = this.pos;
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof BlackboardBlockTile board) {
            level.playSound(null, this.pos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS, 1, 0.8f);
            board.setPixels(this.pixels);
            //updates client
            //set changed also sends a block update
            board.setChanged();
        }
    }
}