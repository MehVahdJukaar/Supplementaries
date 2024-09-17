package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class ServerBoundSetBlackboardPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetBlackboardPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_set_blackboard"), ServerBoundSetBlackboardPacket::new);

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
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        for (byte[] pixel : this.pixels) {
            buf.writeByteArray(pixel);
        }
    }

    @Override
    public void handle(Context context) {
        Player sender = context.getPlayer();
        Level level = Objects.requireNonNull(sender).level();

        BlockPos pos = this.pos;
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof BlackboardBlockTile board) {
            if (board.tryAcceptingClientPixels((ServerPlayer) sender, this.pixels)) {
                //updates client
                //set changed also sends a block update
                board.setChanged();
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}