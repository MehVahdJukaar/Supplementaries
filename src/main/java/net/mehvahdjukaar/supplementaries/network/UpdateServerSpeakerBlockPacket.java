package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class UpdateServerSpeakerBlockPacket{
    private final BlockPos pos;
    private final Component str;
    private final boolean narrator;
    private final double volume;

    public UpdateServerSpeakerBlockPacket(FriendlyByteBuf buf) {

        this.pos = buf.readBlockPos();
        this.str = buf.readComponent();
        this.narrator = buf.readBoolean();
        this.volume = buf.readDouble();
    }

    public UpdateServerSpeakerBlockPacket(BlockPos pos, String str, boolean narrator, double volume) {
        this.pos = pos;
        this.str = new TextComponent(str);
        this.narrator = narrator;
        this.volume = volume;
    }

    public static void buffer(UpdateServerSpeakerBlockPacket message, FriendlyByteBuf buf) {

        buf.writeBlockPos(message.pos);
        buf.writeComponent(message.str);
        buf.writeBoolean(message.narrator);
        buf.writeDouble(message.volume);
    }

    public static void handler(UpdateServerSpeakerBlockPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        Level world = Objects.requireNonNull(ctx.get().getSender()).level;

        ctx.get().enqueueWork(() -> {
            if (world != null) {
                BlockPos pos = message.pos;
                BlockEntity tileentity = world.getBlockEntity(pos);
                if (tileentity instanceof SpeakerBlockTile) {
                    SpeakerBlockTile speaker = (SpeakerBlockTile) tileentity;
                    speaker.message = message.str.getString();
                    speaker.narrator = message.narrator;
                    speaker.volume = message.volume;
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