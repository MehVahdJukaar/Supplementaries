package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
        World world = Objects.requireNonNull(ctx.get().getSender()).level;
        ctx.get().enqueueWork(() -> {
            if (world != null) {
                BlockPos pos = message.pos;
                TileEntity tileentity = world.getBlockEntity(pos);
                if (tileentity instanceof BlackboardBlockTile) {
                    BlackboardBlockTile board = (BlackboardBlockTile) tileentity;
                    world.playSound(null,message.pos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS,1,0.8f);
                    board.pixels = message.pixels;
                    //updates client
                    BlockState state =  world.getBlockState(pos);
                    world.sendBlockUpdated(pos, state, state, 3);
                    tileentity.setChanged();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}