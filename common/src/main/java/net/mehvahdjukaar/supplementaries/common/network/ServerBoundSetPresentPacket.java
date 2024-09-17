package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public record ServerBoundSetPresentPacket(
        BlockPos pos,
        boolean packed,
        String recipient,
        String sender,
        String description) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetPresentPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_set_present"), ServerBoundSetPresentPacket::new);


    public ServerBoundSetPresentPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBoolean(), buf.readUtf(),
                buf.readUtf(), buf.readUtf());
    }


    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.packed);
        buf.writeUtf(this.recipient);
        buf.writeUtf(this.sender);
        buf.writeUtf(this.description);
    }

    @Override
    public void handle(Context context) {
        // server level
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getPlayer());
        Level level = player.level();

        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof PresentBlockTile present) {
            //TODO: sound here
//TODO: check if 2 players cant edit at once of it it needs OnePlyaerInteractable
            present.updateState(this.packed, this.recipient, this.sender, this.description);

            BlockState state = level.getBlockState(pos);
            present.setChanged();
            //also sends new block to clients. maybe not needed since blockstate changes
            level.sendBlockUpdated(pos, state, state, 3);

            //if I'm packing also closes the gui
            if (this.packed) {
                player.doCloseContainer();
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}