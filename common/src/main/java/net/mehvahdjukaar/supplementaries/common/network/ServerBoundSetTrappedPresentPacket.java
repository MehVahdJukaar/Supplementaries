package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ServerBoundSetTrappedPresentPacket implements Message {
    private final BlockPos pos;
    private final boolean packed;

    public ServerBoundSetTrappedPresentPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.packed = buf.readBoolean();
    }

    public ServerBoundSetTrappedPresentPacket(BlockPos pos, boolean packed) {
        this.pos = pos;
        this.packed = packed;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.packed);
    }


    @Override
    public void handle(ChannelHandler.Context context) {
        // server level
        if (context.getSender() instanceof ServerPlayer player) {
            Level level = player.level();

            if (level.hasChunkAt(pos) && level.getBlockEntity(this.pos) instanceof TrappedPresentBlockTile present) {
                //TODO: sound here

                present.updateState(this.packed);

                BlockState state = level.getBlockState(pos);
                present.setChanged();
                //also sends new block to clients. maybe not needed since blockstate changes
                level.sendBlockUpdated(pos, state, state, 3);

                //if I'm packing also closes the gui
                if (this.packed) {
                    player.doCloseContainer();
                }
            }
        }
    }
}