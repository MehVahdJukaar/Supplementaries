package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

//TODO: move to lib
public class ServerBoundSetTextHolderPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetTextHolderPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_set_text_holder"), ServerBoundSetTextHolderPacket::new);

    private final BlockPos pos;
    public final String[][] textHolderLines;

    public ServerBoundSetTextHolderPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.textHolderLines = new String[buf.readVarInt()][];
        for (int i = 0; i < textHolderLines.length; ++i) {
            String[] lines = new String[buf.readVarInt()];
            for (int j = 0; j < lines.length; ++j) {
                lines[j] = buf.readUtf();
            }
            this.textHolderLines[i] = lines;
        }
    }

    public ServerBoundSetTextHolderPacket(BlockPos pos, String[][] holderLines) {
        this.pos = pos;
        this.textHolderLines = holderLines;
    }

    public ServerBoundSetTextHolderPacket(BlockPos pos, String[] lines) {
        this(pos, new String[][]{lines});
    }

    public ServerBoundSetTextHolderPacket(BlockPos pos, String line) {
        this(pos, new String[]{line});
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.textHolderLines.length);
        for (String[] l : this.textHolderLines) {
            buf.writeVarInt(l.length);
            for (var v : l) buf.writeUtf(v);
        }
    }

    @Override
    public void handle(Context context) {
        // text filtering yay
        ServerPlayer sender = (ServerPlayer) context.getPlayer();

        CompletableFuture.supplyAsync(() ->
                Stream.of(textHolderLines)
                        .map(line -> Stream.of(line)
                                .map(ChatFormatting::stripFormatting)
                                .toList()
                        )
                        .map(innerList ->
                                sender.connection.filterTextPacket(innerList))
                        .map(CompletableFuture::join)
                        .toList()
        ).thenAcceptAsync((l) -> {
            this.updateSignText(sender, l);
        }, sender.server);
    }


    private void updateSignText(ServerPlayer player, List<List<FilteredText>> filteredText) {
        player.resetLastActionTime();
        Level level = player.level();

        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof ITextHolderProvider te) {
            if (te.tryAcceptingClientText(pos, player, filteredText)) {
                BlockEntity be = (BlockEntity) te;
                be.setChanged();
                level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}