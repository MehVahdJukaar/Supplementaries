package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.blocks.tiles.BlackboardBlockTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class UpdateServerBlackboardPacket {
    private final BlockPos pos;
    private final byte[][] pixels;

    public UpdateServerBlackboardPacket(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.pixels = new byte[16][16];
        for(int i = 0; i<this.pixels.length; i++) {
            this.pixels[i]=buf.readByteArray();
        }

    }

    public UpdateServerBlackboardPacket(BlockPos pos, byte[][] pixels) {
        this.pos = pos;
        this.pixels = pixels;
    }

    public static void buffer(UpdateServerBlackboardPacket message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        for(int i = 0; i<message.pixels.length; i++) {
            buf.writeByteArray(message.pixels[i]);
        }
    }

    public static void handler(UpdateServerBlackboardPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = Objects.requireNonNull(ctx.get().getSender()).world;
        ctx.get().enqueueWork(() -> {
            if (world != null) {
                TileEntity tileentity = world.getTileEntity(message.pos);
                if (tileentity instanceof BlackboardBlockTile) {
                    BlackboardBlockTile board = (BlackboardBlockTile) tileentity;
                    board.pixels= message.pixels;
                    tileentity.markDirty();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}