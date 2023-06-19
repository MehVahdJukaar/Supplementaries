package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.stream.Stream;

//TODO: move to lib
public class ServerBoundSetTextHolderPacket implements Message {
    private final BlockPos pos;
    private final int index;
    public final String[] lines;
    public final int size;

    public ServerBoundSetTextHolderPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.index = buf.readVarInt();
        this.size = buf.readVarInt();
        this.lines = new String[this.size];
        for (int i = 0; i < this.size; ++i) {
            this.lines[i] = buf.readUtf();
        }
    }

    public ServerBoundSetTextHolderPacket(BlockPos pos, int index, String[] lines) {
        this.pos = pos;
        this.size = lines.length;
        this.lines = lines;
        this.index = index;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.index);
        buf.writeVarInt(this.size);
        for (int i = 0; i < this.size; ++i) {
            buf.writeUtf(this.lines[i]);
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // text filtering yay
        List<String> list = Stream.of(lines).map(ChatFormatting::stripFormatting).toList();
        ServerPlayer sender = (ServerPlayer) context.getSender();
        sender.connection.filterTextPacket(list).thenAcceptAsync((l) -> {
            this.updateSignText(sender, l);
        }, sender.server);
    }


    private void updateSignText(ServerPlayer player, List<FilteredText> filteredText) {
        player.resetLastActionTime();
        Level level = player.level();

        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof ITextHolderProvider te) {
            if (te.tryAcceptingClientText(this.index, player, filteredText)) {
                BlockEntity be = (BlockEntity) te;
                level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
            }
        }
    }

}