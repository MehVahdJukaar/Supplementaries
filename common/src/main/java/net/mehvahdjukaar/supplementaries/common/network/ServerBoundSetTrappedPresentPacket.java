package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

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
        // server world
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getSender());
        Level world = player.level;

        BlockPos pos = this.pos;
        if (world.getBlockEntity(this.pos) instanceof TrappedPresentBlockTile present) {
            //TODO: sound here

            present.updateState(this.packed);

            BlockState state = world.getBlockState(pos);
            present.setChanged();
            //also sends new block to clients. maybe not needed since blockstate changes
            world.sendBlockUpdated(pos, state, state, 3);

            //if I'm packing also closes the gui
            if (this.packed) {
                player.doCloseContainer();
            }
        }
    }
}