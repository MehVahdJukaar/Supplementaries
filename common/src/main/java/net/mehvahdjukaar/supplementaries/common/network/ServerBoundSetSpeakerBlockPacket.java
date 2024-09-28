package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.level.Level;

public class ServerBoundSetSpeakerBlockPacket implements Message {
    private final BlockPos pos;
    private final String str;
    private final SpeakerBlockTile.Mode mode;
    private final double volume;

    public ServerBoundSetSpeakerBlockPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.str = buf.readUtf();
        this.mode = SpeakerBlockTile.Mode.values()[buf.readByte()];
        this.volume = buf.readDouble();
    }

    public ServerBoundSetSpeakerBlockPacket(BlockPos pos, String str, SpeakerBlockTile.Mode mode, double volume) {
        this.pos = pos;
        this.str = str;
        this.mode = mode;
        this.volume = volume;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.str);
        buf.writeByte(this.mode.ordinal());
        buf.writeDouble(this.volume);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server level
        if(context.getSender() instanceof ServerPlayer sender) {
            Level level = sender.level();
            BlockPos pos = this.pos;
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SpeakerBlockTile speaker) {
                speaker.setVolume(this.volume);
                speaker.setMode(this.mode);
                sender.connection.filterTextPacket(this.str).thenAcceptAsync((l) -> {
                    this.updateSpeakerText(sender, l);
                }, sender.server);
            }
        }
    }

    private void updateSpeakerText(ServerPlayer player, FilteredText filteredText) {
        player.resetLastActionTime();
        Level level = player.level();

        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SpeakerBlockTile be) {
            if (be.tryAcceptingClientText(player, filteredText)) {
                level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
                be.setChanged();
            }
        }
    }
}