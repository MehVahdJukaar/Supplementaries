package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ServerBoundSetSpeakerBlockPacket implements Message {
    private final BlockPos pos;
    private final Component str;
    private final SpeakerBlockTile.Mode mode;
    private final double volume;

    public ServerBoundSetSpeakerBlockPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.str = buf.readComponent();
        this.mode = SpeakerBlockTile.Mode.values()[buf.readByte()];
        this.volume = buf.readDouble();
    }

    public ServerBoundSetSpeakerBlockPacket(BlockPos pos, String str, SpeakerBlockTile.Mode mode, double volume) {
        this.pos = pos;
        this.str = Component.literal(str);
        this.mode = mode;
        this.volume = volume;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeComponent(this.str);
        buf.writeByte(this.mode.ordinal());
        buf.writeDouble(this.volume);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server world
        Level world = Objects.requireNonNull(context.getSender()).level();

        BlockPos pos = this.pos;
        if (world.getBlockEntity(pos) instanceof SpeakerBlockTile speaker) {
            speaker.setVolume(this.volume);
            speaker.setMode(this.mode);
            speaker.setMessage(this.str.getString());

            //updates client
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, 3);
            speaker.setChanged();
        }
    }
}