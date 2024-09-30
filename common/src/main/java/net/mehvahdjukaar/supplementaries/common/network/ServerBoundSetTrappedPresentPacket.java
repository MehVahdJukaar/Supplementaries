package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record ServerBoundSetTrappedPresentPacket(BlockPos pos, boolean packed) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetTrappedPresentPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_set_trapped_present"), ServerBoundSetTrappedPresentPacket::new);

    public ServerBoundSetTrappedPresentPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBoolean());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.packed);
    }

    @Override
    public void handle(Context context) {
        // server level
        if (context.getPlayer() instanceof ServerPlayer player) {
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}