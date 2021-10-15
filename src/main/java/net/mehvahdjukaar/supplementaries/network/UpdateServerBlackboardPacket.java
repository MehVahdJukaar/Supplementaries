package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class UpdateServerBlackboardPacket {
    private final BlockPos pos;
    private final byte[][] pixels;

    public UpdateServerBlackboardPacket(FriendlyByteBuf buf) {
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

    public static void buffer(UpdateServerBlackboardPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        for(int i = 0; i<message.pixels.length; i++) {
            buf.writeByteArray(message.pixels[i]);
        }
    }

    public static void handler(UpdateServerBlackboardPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        Level world = Objects.requireNonNull(ctx.get().getSender()).level;
        ctx.get().enqueueWork(() -> {
            if (world != null) {
                BlockPos pos = message.pos;
                BlockEntity tileentity = world.getBlockEntity(pos);
                if (tileentity instanceof BlackboardBlockTile) {
                    BlackboardBlockTile board = (BlackboardBlockTile) tileentity;
                    world.playSound(null,message.pos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS,1,0.8f);
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