package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundSetPresentPacket {
    private final BlockPos pos;
    private final boolean packed;
    private final String sender;
    private final String recipient;
    private final String description;

    public ServerBoundSetPresentPacket(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.packed = buf.readBoolean();
        this.recipient = buf.readUtf();
        this.sender = buf.readUtf();
        this.description = buf.readUtf();
    }

    public ServerBoundSetPresentPacket(BlockPos pos, boolean packed, String recipient, String sender, String description) {
        this.pos = pos;
        this.packed = packed;
        this.recipient = recipient;
        this.sender = sender;
        this.description = description;
    }

    public static void buffer(ServerBoundSetPresentPacket message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeBoolean(message.packed);
        buf.writeUtf(message.recipient);
        buf.writeUtf(message.sender);
        buf.writeUtf(message.description);
    }

    public static void handler(ServerBoundSetPresentPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        ServerPlayerEntity player = Objects.requireNonNull(ctx.get().getSender());
        World world = player.level;
        ctx.get().enqueueWork(() -> {
            BlockPos pos = message.pos;
            TileEntity te = world.getBlockEntity(message.pos);
            if (te instanceof PresentBlockTile) {
                //TODO: sound here
                PresentBlockTile present = ((PresentBlockTile) te);
                present.updateState(message.packed, message.recipient, message.sender, message.description);

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