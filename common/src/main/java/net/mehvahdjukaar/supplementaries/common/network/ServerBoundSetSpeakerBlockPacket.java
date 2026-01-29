package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.level.Level;

public record ServerBoundSetSpeakerBlockPacket(
        BlockPos pos,
        String str,
        SpeakerBlockTile.Mode mode,
        double volume) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetSpeakerBlockPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_set_speaker"), ServerBoundSetSpeakerBlockPacket::new);

    public ServerBoundSetSpeakerBlockPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf(), buf.readEnum(SpeakerBlockTile.Mode.class), buf.readDouble());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.str);
        buf.writeEnum(this.mode);
        buf.writeDouble(this.volume);
    }

    @Override
    public void handle(Context context) {
        // server level
        if (context.getPlayer() instanceof ServerPlayer sender) {
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}