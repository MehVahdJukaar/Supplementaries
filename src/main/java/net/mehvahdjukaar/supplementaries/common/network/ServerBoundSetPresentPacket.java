package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundSetPresentPacket {
    private final BlockPos pos;
    private final boolean packed;
    private final String sender;
    private final String recipient;

    public ServerBoundSetPresentPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.packed = buf.readBoolean();
        this.recipient = buf.readUtf();
        this.sender = buf.readUtf();
    }

    public ServerBoundSetPresentPacket(BlockPos pos, boolean packed, String recipient, String sender) {
        this.pos = pos;
        this.packed = packed;
        this.recipient = recipient;
        this.sender = sender;
    }

    public static void buffer(ServerBoundSetPresentPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeBoolean(message.packed);
        buf.writeUtf(message.recipient);
        buf.writeUtf(message.sender);
    }

    public static void handler(ServerBoundSetPresentPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
        Level world = player.level;
        ctx.get().enqueueWork(() -> {
            BlockPos pos = message.pos;
            if (world.getBlockEntity(message.pos) instanceof PresentBlockTile present) {
                //TODO: sound here

                present.updateState(message.recipient, message.sender, message.packed);

                BlockState state = world.getBlockState(pos);
                present.setChanged();
                //also sends new block to clients
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