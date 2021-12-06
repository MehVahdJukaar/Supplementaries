package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundSetSpeakerBlockPacket {
    private final BlockPos pos;
    private final Component str;
    private final boolean narrator;
    private final double volume;

    public ServerBoundSetSpeakerBlockPacket(FriendlyByteBuf buf) {

        this.pos = buf.readBlockPos();
        this.str = buf.readComponent();
        this.narrator = buf.readBoolean();
        this.volume = buf.readDouble();
    }

    public ServerBoundSetSpeakerBlockPacket(BlockPos pos, String str, boolean narrator, double volume) {
        this.pos = pos;
        this.str = new TextComponent(str);
        this.narrator = narrator;
        this.volume = volume;
    }

    public static void buffer(ServerBoundSetSpeakerBlockPacket message, FriendlyByteBuf buf) {

        buf.writeBlockPos(message.pos);
        buf.writeComponent(message.str);
        buf.writeBoolean(message.narrator);
        buf.writeDouble(message.volume);
    }

    public static void handler(ServerBoundSetSpeakerBlockPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        Level world = Objects.requireNonNull(ctx.get().getSender()).level;

        ctx.get().enqueueWork(() -> {
            BlockPos pos = message.pos;
            if (world.getBlockEntity(pos) instanceof SpeakerBlockTile speaker) {
                speaker.message = message.str.getString();
                speaker.narrator = message.narrator;
                speaker.volume = message.volume;
                //updates client
                BlockState state = world.getBlockState(pos);
                world.sendBlockUpdated(pos, state, state, 3);
                speaker.setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}