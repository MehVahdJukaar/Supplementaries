package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundSetTrappedPresentPacket {
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

    public static void buffer(ServerBoundSetTrappedPresentPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeBoolean(message.packed);
    }

    public static void handler(ServerBoundSetTrappedPresentPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
        Level world = player.level;
        ctx.get().enqueueWork(() -> {
            BlockPos pos = message.pos;
            if (world.getBlockEntity(message.pos) instanceof TrappedPresentBlockTile present) {
                //TODO: sound here

                present.updateState(message.packed);

                BlockState state = world.getBlockState(pos);
                present.setChanged();
                //also sends new block to clients. maybe not needed since blockstate changes
                world.sendBlockUpdated(pos, state, state, 3);

                //if I'm packing also closes the gui
                if (message.packed) {
                    player.doCloseContainer();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}